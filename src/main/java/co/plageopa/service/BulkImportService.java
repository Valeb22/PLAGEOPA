package co.plageopa.service;

import co.plageopa.DTO.ImportPreviewResponse;
import co.plageopa.DTO.ImportPreviewRow;
import co.plageopa.DTO.ImportResult;
import co.plageopa.domain.*;
import co.plageopa.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class BulkImportService {

  private final ProductorRepository productorRepo;
  private final FincaRepository fincaRepo;
  private final CultivoRepository cultivoRepo;
  private final VeredaRepository veredaRepo;

  private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

  // Para cortar cuando el Excel “sigue infinito” por formato
  private static final int MAX_CONSECUTIVE_BLANKS = 30;

  public BulkImportService(ProductorRepository productorRepo,
                           FincaRepository fincaRepo,
                           CultivoRepository cultivoRepo,
                           VeredaRepository veredaRepo) {
    this.productorRepo = productorRepo;
    this.fincaRepo = fincaRepo;
    this.cultivoRepo = cultivoRepo;
    this.veredaRepo = veredaRepo;
  }

  // =========================
  // PREVIEW
  // =========================
  public ImportPreviewResponse preview(MultipartFile file, int limit) throws Exception {
    List<ImportPreviewRow> out = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook wb = new XSSFWorkbook(is)) {

      Sheet sh = wb.getSheetAt(0);
      int last = sh.getLastRowNum();

      int startIdx = resolveDataStartRowIdx(sh);

      int ok = 0, err = 0;
      int blanks = 0;

      for (int i = startIdx; i <= last; i++) {
        Row r = sh.getRow(i);

        if (r == null || isSkippableRow(r)) {
          blanks++;
          if (blanks >= MAX_CONSECUTIVE_BLANKS) break;
          continue;
        }
        blanks = 0;

        ImportPreviewRow row = parseRow(r, i + 1);
        validateRow(row);

        if (row.ok) ok++; else err++;
        out.add(row);

        if (limit > 0 && out.size() >= limit) break;
      }

      return new ImportPreviewResponse(out.size(), ok, err, out);
    }
  }

  // =========================
  // APPLY
  // =========================
  public ImportResult apply(MultipartFile file) throws Exception {

    int insertedProd = 0, updatedProd = 0;
    int insertedFinc = 0, updatedFinc = 0;
    int insertedCult = 0, updatedCult = 0;
    int errorRows = 0;
    int totalRows = 0;

    Map<String, Productor> cacheProd = new HashMap<>();
    Map<UUID, Finca> cacheFinca = new HashMap<>();
    Map<String, Vereda> cacheVereda = new HashMap<>();

    try (InputStream is = file.getInputStream();
         Workbook wb = new XSSFWorkbook(is)) {

      Sheet sh = wb.getSheetAt(0);
      int last = sh.getLastRowNum();

      int startIdx = resolveDataStartRowIdx(sh);
      int blanks = 0;

      for (int i = startIdx; i <= last; i++) {
        Row rr = sh.getRow(i);

        if (rr == null || isSkippableRow(rr)) {
          blanks++;
          if (blanks >= MAX_CONSECUTIVE_BLANKS) break;
          continue;
        }
        blanks = 0;

        totalRows++;

        ImportPreviewRow r = parseRow(rr, i + 1);
        validateRow(r);

        if (!r.ok) {
          errorRows++;
          continue;
        }

        // -------- Productor (UPSERT por cédula) --------
        Productor prod = cacheProd.get(r.cedula);
        if (prod == null) prod = productorRepo.findByCedula(r.cedula).orElse(null);

        boolean asoc = "SI".equalsIgnoreCase(r.perteneceAsociacion);

        if (prod == null) {
          prod = new Productor();
          prod.setCedula(r.cedula);
          prod.setNombre(r.nombre);
          prod.setTelefono(emptyToNull(r.telefono));
          prod.setGenero(r.genero);
          prod.setPerteneceAsociacion(asoc);
          prod.setNombreAsociacion(asoc ? emptyToNull(r.nombreAsociacion) : null);
          prod = productorRepo.save(prod);
          insertedProd++;
        } else {
          prod.setNombre(r.nombre);
          prod.setTelefono(emptyToNull(r.telefono));
          prod.setGenero(r.genero);
          prod.setPerteneceAsociacion(asoc);
          prod.setNombreAsociacion(asoc ? emptyToNull(r.nombreAsociacion) : null);
          prod = productorRepo.save(prod);
          updatedProd++;
        }
        cacheProd.put(r.cedula, prod);

        // -------- Vereda (opcional) --------
        Vereda vereda = null;
        if (r.codigoVereda != null && !r.codigoVereda.isBlank()) {
          vereda = cacheVereda.get(r.codigoVereda);
          if (vereda == null) {
            vereda = veredaRepo.findByCodigoCorto(r.codigoVereda).orElse(null);
            if (vereda != null) cacheVereda.put(r.codigoVereda, vereda);
          }
        }

        // -------- Finca (UPSERT)
        // - si viene globalid => ese manda
        // - si no viene => buscar por cedula + lon + lat para no duplicar
        UUID gid;
        Finca finca;

        if (r.globalid != null && !r.globalid.isBlank()) {
          gid = UUID.fromString(r.globalid.trim());

          finca = cacheFinca.get(gid);
          if (finca == null) finca = fincaRepo.findByGlobalid(gid).orElse(null);

        } else {
          finca = fincaRepo.findOneByCedulaAndLonLat(r.cedula, r.lon, r.lat).orElse(null);
          if (finca != null) {
            gid = finca.getGlobalid(); // ya existe
          } else {
            gid = UUID.randomUUID();
          }
        }

        if (finca == null) {
          finca = new Finca();
          finca.setGlobalid(gid);
          finca.setProductor(prod);
          finca.setAreaTotal(bd(r.areaTotal));
          finca.setTipoActividad(r.tipoActividad);

          Point p = gf.createPoint(new Coordinate(r.lon, r.lat));
          p.setSRID(4326);
          finca.setGeom(p);

          finca.setVereda(vereda); // opcional (null permitido)
          finca = fincaRepo.save(finca);
          insertedFinc++;
        } else {
          finca.setProductor(prod);
          finca.setAreaTotal(bd(r.areaTotal));
          finca.setTipoActividad(r.tipoActividad);
          finca.setVereda(vereda); // opcional
          finca = fincaRepo.save(finca);
          updatedFinc++;
        }

        cacheFinca.put(gid, finca);

        // -------- Cultivo (UPSERT por finca + nombre + variedad) --------
        String nom = r.nombreCultivo == null ? null : r.nombreCultivo.trim();
        String var = emptyToNull(r.variedad);

        Cultivo existing;
        if (var == null) {
          existing = cultivoRepo
              .findFirstByFincaIdAndNombreCultivoIgnoreCaseAndVariedadIsNull(finca.getId(), nom)
              .orElse(null);
        } else {
          existing = cultivoRepo
              .findFirstByFincaIdAndNombreCultivoIgnoreCaseAndVariedadIgnoreCase(finca.getId(), nom, var)
              .orElse(null);
        }

        if (existing == null) {
          Cultivo c = new Cultivo();
          c.setFinca(finca);
          c.setNombreCultivo(nom);
          c.setVariedad(var);
          c.setArea(bd(r.areaCultivo));
          cultivoRepo.save(c);
          insertedCult++;
        } else {
          // Reemplaza / actualiza área (y lo que quieras)
          existing.setArea(bd(r.areaCultivo));
          cultivoRepo.save(existing);
          updatedCult++;
        }
      }
    }

    return new ImportResult(
        totalRows,
        insertedProd,
        updatedProd,
        insertedFinc,
        updatedFinc,
        insertedCult,  // si tu DTO no tiene updatedCult, déjalo así
        errorRows
    );
  }

  // =========================
  // ✅ Detecta dónde empieza la data
  // =========================
  private int resolveDataStartRowIdx(Sheet sh) {
    int scanMax = Math.min(sh.getLastRowNum(), 80);

    for (int i = 0; i <= scanMax; i++) {
      Row r = sh.getRow(i);
      if (r == null) continue;

      String c0 = norm(s(r, 0));
      String c1 = norm(s(r, 1));
      String c12 = norm(s(r, 12));

      boolean looksLikeHeader =
          (c0.startsWith("cedula") || c0.contains("cedula")) &&
          (c1.contains("nombre") || c1.contains("productor")) &&
          (c12.contains("cultivo") || c12.contains("nombre_cultivo"));

      if (looksLikeHeader) {
        // header + 1 = descripción, data = header + 2
        return Math.min(i + 2, sh.getLastRowNum());
      }
    }

    // fallback: fila 6 (índice 5)
    return 5;
  }

  private static String norm(String s) {
    if (s == null) return "";
    return s.trim().toLowerCase(Locale.ROOT);
  }

  // Salta: vacías, instrucciones, header, descripciones
  private boolean isSkippableRow(Row r) {
    String ced = s(r, 0);
    String cul = s(r, 12);

    // vacía “real”
    boolean empty = (ced == null || ced.isBlank()) && (cul == null || cul.isBlank());
    if (empty) return true;

    // instrucciones (• ...)
    if (ced != null && ced.trim().startsWith("•")) return true;

    // header (cedula*)
    if (ced != null && norm(ced).startsWith("cedula")) {
      String c1 = norm(s(r, 1));
      if (c1.contains("nombre")) return true;
    }

    // descripción
    if (ced != null && norm(ced).contains("cédula del productor")) return true;
    if (ced != null && norm(ced).contains("cedula del productor")) return true;

    return false;
  }

  // =========================
  // Parsing & Validación
  // =========================
  private ImportPreviewRow parseRow(Row r, int rowNum) {
    ImportPreviewRow o = new ImportPreviewRow();
    o.rowNum = rowNum;

    o.cedula = s(r, 0);
    o.nombre = s(r, 1);
    o.telefono = s(r, 2);
    o.genero = s(r, 3);
    o.perteneceAsociacion = s(r, 4);
    o.nombreAsociacion = s(r, 5);

    o.globalid = s(r, 6);

    o.areaTotal = d(r, 7);
    o.tipoActividad = s(r, 8);
    o.lon = d(r, 9);
    o.lat = d(r, 10);
    o.codigoVereda = s(r, 11);

    o.nombreCultivo = s(r, 12);
    o.variedad = s(r, 13);
    o.areaCultivo = d(r, 14);

    return o;
  }

  private void validateRow(ImportPreviewRow r) {
    r.errors.clear();

    req(r, r.cedula, "cedula");
    req(r, r.nombre, "nombre");
    req(r, r.genero, "genero");
    req(r, r.perteneceAsociacion, "pertenece_asociacion");
    req(r, r.tipoActividad, "tipo_actividad");
    req(r, r.nombreCultivo, "nombre_cultivo");

    if (r.areaTotal == null) r.errors.add("area_total es obligatorio");
    if (r.areaCultivo == null) r.errors.add("area_cultivo es obligatorio");
    if (r.lon == null) r.errors.add("lon es obligatorio");
    if (r.lat == null) r.errors.add("lat es obligatorio");

    if (r.areaTotal != null && r.areaTotal < 0) r.errors.add("area_total no puede ser negativa");
    if (r.areaCultivo != null && r.areaCultivo < 0) r.errors.add("area_cultivo no puede ser negativa");

    if (r.lon != null && (r.lon < -180 || r.lon > 180)) r.errors.add("lon fuera de rango [-180,180]");
    if (r.lat != null && (r.lat < -90 || r.lat > 90)) r.errors.add("lat fuera de rango [-90,90]");

    if (r.genero != null && !(r.genero.equalsIgnoreCase("Femenino") || r.genero.equalsIgnoreCase("Masculino"))) {
      r.errors.add("genero debe ser Femenino o Masculino");
    }

    if (r.perteneceAsociacion != null && !(r.perteneceAsociacion.equalsIgnoreCase("SI") || r.perteneceAsociacion.equalsIgnoreCase("NO"))) {
      r.errors.add("pertenece_asociacion debe ser SI o NO");
    }

    boolean asoc = r.perteneceAsociacion != null && r.perteneceAsociacion.equalsIgnoreCase("SI");
    if (asoc && (r.nombreAsociacion == null || r.nombreAsociacion.isBlank())) {
      r.errors.add("nombre_asociacion es obligatorio si pertenece_asociacion=SI");
    }

    if (r.globalid != null && !r.globalid.isBlank()) {
      try { UUID.fromString(r.globalid.trim()); }
      catch (Exception ex) { r.errors.add("globalid inválido (debe ser UUID)"); }
    }

    r.ok = r.errors.isEmpty();
  }

  private static void req(ImportPreviewRow r, String v, String name) {
    if (v == null || v.isBlank()) r.errors.add(name + " es obligatorio");
  }

  // Strings: soporta cédula numérica
  private static String s(Row r, int idx) {
    Cell c = r.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (c == null) return null;

    if (c.getCellType() == CellType.STRING) {
      String v = c.getStringCellValue();
      return v == null ? null : v.trim();
    }

    if (c.getCellType() == CellType.NUMERIC) {
      double n = c.getNumericCellValue();
      long asLong = (long) n;
      if (Math.abs(n - asLong) < 0.0000001) return String.valueOf(asLong);
      return String.valueOf(n);
    }

    c.setCellType(CellType.STRING);
    String v = c.getStringCellValue();
    return v == null ? null : v.trim();
  }

  private static Double d(Row r, int idx) {
    Cell c = r.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (c == null) return null;

    if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();

    if (c.getCellType() == CellType.STRING) {
      String t = c.getStringCellValue();
      if (t == null || t.trim().isEmpty()) return null;
      try { return Double.parseDouble(t.trim().replace(",", ".")); }
      catch (Exception e) { return null; }
    }

    return null;
  }

  private static String emptyToNull(String s) {
    return (s == null || s.isBlank()) ? null : s.trim();
  }

  private static BigDecimal bd(Double v) {
    if (v == null) return null;
    return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
  }
}
