package co.plageopa.service.impl;

import co.plageopa.domain.*;
import co.plageopa.DTO.*;
import co.plageopa.exception.NotFoundException;
import co.plageopa.repository.*;
import co.plageopa.service.RegistroService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class RegistroServiceImpl implements RegistroService {

  private final ProductorRepository productorRepo;
  private final FincaRepository fincaRepo;
  private final CultivoRepository cultivoRepo;
  private final VeredaRepository veredaRepo;
  private final LogRepository logRepo;
  

  @PersistenceContext
  private EntityManager em;

  private final GeometryFactory gf;

  public RegistroServiceImpl(ProductorRepository productorRepo,
                             FincaRepository fincaRepo,
                             CultivoRepository cultivoRepo,
                             VeredaRepository veredaRepo,
                             LogRepository logRepo,
                             GeometryFactory gf) {
    this.productorRepo = productorRepo;
    this.fincaRepo = fincaRepo;
    this.cultivoRepo = cultivoRepo;
    this.veredaRepo = veredaRepo;
    this.logRepo = logRepo;
    this.gf = gf;
  }

  @Override
  public Map<String, Object> crearRegistro(RegistroCreateDto dto, Integer userId) {

    if (productorRepo.existsByCedula(dto.getProductor().getCedula())) {
      throw new IllegalArgumentException("La cédula ya existe: " + dto.getProductor().getCedula());
    }

    double lon = dto.getFinca().getLon();
    double lat = dto.getFinca().getLat();
    if (fincaRepo.existsByLonLat(lon, lat)) {
      throw new IllegalArgumentException("Ya existe una finca en ese punto (lon/lat).");
    }

    Productor p = new Productor();
    p.setCedula(dto.getProductor().getCedula());
    p.setNombre(dto.getProductor().getNombre());
    p.setTelefono(dto.getProductor().getTelefono());
    p.setGenero(dto.getProductor().getGenero());
    p.setPerteneceAsociacion(dto.getProductor().isPerteneceAsociacion());
    p.setNombreAsociacion(dto.getProductor().getNombreAsociacion());
    p = productorRepo.save(p);

    saveLog(userId, "productores", "INSERT", p.getId(),
      "Se creó el productor '" + nv(p.getNombre()) + "' (cédula " + nv(p.getCedula()) + ")");

    FincaDto f = dto.getFinca();
    Finca finca = new Finca();
    finca.setGlobalid(UUID.randomUUID());
    finca.setProductor(p);

    if (f.getVeredaCodigo() != null && !f.getVeredaCodigo().isBlank()) {
      Vereda v = veredaRepo.findFirstByCodigoCorto(f.getVeredaCodigo())
        .orElseThrow(() -> new NotFoundException("Vereda no encontrada: " + f.getVeredaCodigo()));
      finca.setVereda(v);
    } else {
      finca.setVereda(null);
    }

    finca.setAreaTotal(f.getAreaTotal() == null ? null : BigDecimal.valueOf(f.getAreaTotal()));
    finca.setTipoActividad(f.getTipoActividad());

    Point pt = gf.createPoint(new Coordinate(lon, lat));
    pt.setSRID(4326);
    finca.setGeom(pt);

    finca = fincaRepo.save(finca);

    saveLog(userId, "fincas", "INSERT", finca.getId(),
      "Se creó finca globalId=" + finca.getGlobalid() +
        ", área=" + fmtNum(finca.getAreaTotal()) + " ha" +
        ", actividad='" + nv(finca.getTipoActividad()) + "'" +
        (finca.getGeom() != null ? (", lon=" + finca.getGeom().getX() + ", lat=" + finca.getGeom().getY()) : "") +
        (finca.getVereda() != null ? (", vereda=" + nv(finca.getVereda().getCodigoCorto())) : ""));

    if (dto.getCultivos() != null) {
      for (CultivoDto c : dto.getCultivos()) {
        Cultivo cu = new Cultivo();
        cu.setFinca(finca);
        cu.setNombreCultivo(c.getNombreCultivo());
        cu.setVariedad(c.getVariedad());
        cu.setArea(c.getArea() == null ? null : BigDecimal.valueOf(c.getArea()));
        cu = cultivoRepo.save(cu);

        saveLog(userId, "cultivos", "INSERT", cu.getId(),
          "Se creó cultivo '" + nv(cu.getNombreCultivo()) + "', variedad='" + nv(cu.getVariedad()) +
            "', área=" + fmtNum(cu.getArea()));
      }
    }

    return Map.of(
      "ok", true,
      "productor_id", p.getId(),
      "finca_id", finca.getId(),
      "globalid", finca.getGlobalid().toString()
    );
  }

  @Override
  public Map<String, Object> actualizarPorCedula(String cedula, RegistroUpdateDto dto, Integer userId) {

    Productor productor = productorRepo.findByCedula(cedula)
      .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedula));

    // ---------------- PRODUCTOR (partial update) ----------------
    if (dto.getProductor() != null) {
      ProductorUpdateDto pd = dto.getProductor();

      List<String> cambios = new ArrayList<>();
      if (pd.getNombre() != null) {
        addChange(cambios, "nombre", productor.getNombre(), pd.getNombre());
        productor.setNombre(pd.getNombre());
      }
      if (pd.getTelefono() != null) {
        addChange(cambios, "telefono", productor.getTelefono(), pd.getTelefono());
        productor.setTelefono(pd.getTelefono());
      }
      if (pd.getPerteneceAsociacion() != null) {
        addChange(cambios, "perteneceAsociacion", productor.isPerteneceAsociacion(), pd.getPerteneceAsociacion());
        productor.setPerteneceAsociacion(pd.getPerteneceAsociacion());
      }
      if (pd.getNombreAsociacion() != null) {
        addChange(cambios, "nombreAsociacion", productor.getNombreAsociacion(), pd.getNombreAsociacion());
        productor.setNombreAsociacion(pd.getNombreAsociacion());
      }

      productorRepo.save(productor);
      saveLog(userId, "productores", "UPDATE", productor.getId(), "Actualizó → " + joinChanges(cambios));
    }

    // ---------------- FINCA target ----------------
    List<Finca> fincas = fincaRepo.findByProductorId(productor.getId());
    if (fincas.isEmpty()) throw new NotFoundException("El productor no tiene fincas.");

    Finca fincaTarget = fincas.get(0);

    if (dto.getFinca() != null && dto.getFinca().getGlobalid() != null && !dto.getFinca().getGlobalid().isBlank()) {
      UUID gid = UUID.fromString(dto.getFinca().getGlobalid());
      fincaTarget = fincas.stream()
        .filter(ff -> gid.equals(ff.getGlobalid()))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Finca con ese globalid no pertenece al productor"));
    } else if (fincas.size() > 1 && dto.getFinca() != null) {
      throw new IllegalArgumentException("El productor tiene varias fincas. Especifique 'globalid' en 'finca'.");
    }

    // ---------------- FINCA (partial update) ----------------
    if (dto.getFinca() != null) {
      FincaUpdateDto fu = dto.getFinca();
      List<String> cambios = new ArrayList<>();

      // ✅ lon/lat: ambos o ninguno (si viene solo uno => 400)
      boolean lonProvided = fu.getLon() != null;
      boolean latProvided = fu.getLat() != null;

      if (lonProvided ^ latProvided) { // XOR
        throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Debe enviar lon y lat juntos (ambos o ninguno)."
        );
      }

      if (lonProvided && latProvided) {
        boolean existeOtro = fincaRepo.existsByLonLat(fu.getLon(), fu.getLat());
        boolean esMismo = fincaTarget.getGeom() != null &&
          Double.compare(fincaTarget.getGeom().getX(), fu.getLon()) == 0 &&
          Double.compare(fincaTarget.getGeom().getY(), fu.getLat()) == 0;

        if (existeOtro && !esMismo) {
          throw new IllegalArgumentException("Ya existe otra finca registrada en ese punto (lon/lat).");
        }

        if (!esMismo) {
          String antes = (fincaTarget.getGeom() == null) ? "null" :
            "(" + fincaTarget.getGeom().getX() + ", " + fincaTarget.getGeom().getY() + ")";
          String despues = "(" + fu.getLon() + ", " + fu.getLat() + ")";
          cambios.add("ubicación lon/lat: '" + antes + "' → '" + despues + "'");

          Point nuevo = gf.createPoint(new Coordinate(fu.getLon(), fu.getLat()));
          nuevo.setSRID(4326);
          fincaTarget.setGeom(nuevo);
        }
      }

      if (fu.getAreaTotal() != null) {
        addChange(cambios, "areaTotal(ha)", fmtNum(fincaTarget.getAreaTotal()), fu.getAreaTotal());
        fincaTarget.setAreaTotal(BigDecimal.valueOf(fu.getAreaTotal()));
      }
      if (fu.getTipoActividad() != null) {
        addChange(cambios, "tipoActividad", fincaTarget.getTipoActividad(), fu.getTipoActividad());
        fincaTarget.setTipoActividad(fu.getTipoActividad());
      }

      if (fu.getCodigoVereda() != null) {
        String veredaAntes = fincaTarget.getVereda() == null ? null : fincaTarget.getVereda().getCodigoCorto();
        String veredaDespues;

        if (fu.getCodigoVereda().isBlank()) {
          veredaDespues = null;
          fincaTarget.setVereda(null);
        } else {
          Vereda v = veredaRepo.findFirstByCodigoCorto(fu.getCodigoVereda())
            .orElseThrow(() -> new NotFoundException("Vereda no encontrada: " + fu.getCodigoVereda()));
          fincaTarget.setVereda(v);
          veredaDespues = v.getCodigoCorto();
        }
        addChange(cambios, "veredaCodigo", veredaAntes, veredaDespues);
      }

      fincaRepo.save(fincaTarget);
      saveLog(userId, "fincas", "UPDATE", fincaTarget.getId(), "Actualizó → " + joinChanges(cambios));
    }

    // ---------------- CULTIVOS UPSERT (create/update) ----------------
    if (dto.getCultivosUpsert() != null) {
      for (CultivoUpsertDto cu : dto.getCultivosUpsert()) {

        if (cu.getId() == null) {
          // CREATE -> nombre requerido
          if (cu.getNombreCultivo() == null || cu.getNombreCultivo().isBlank()) {
            throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "nombreCultivo es obligatorio para crear cultivo."
            );
          }

          Cultivo c = new Cultivo();
          c.setFinca(fincaTarget);
          c.setNombreCultivo(cu.getNombreCultivo());
          c.setVariedad(cu.getVariedad());
          c.setArea(cu.getArea() == null ? null : BigDecimal.valueOf(cu.getArea()));
          c = cultivoRepo.save(c);

          saveLog(userId, "cultivos", "INSERT", c.getId(),
            "Se creó cultivo '" + nv(c.getNombreCultivo()) + "', variedad='" + nv(c.getVariedad()) +
              "', área=" + fmtNum(c.getArea()));

        } else {
          // UPDATE -> campos opcionales
          Cultivo c = cultivoRepo.findById(cu.getId())
            .orElseThrow(() -> new NotFoundException("Cultivo no encontrado: id=" + cu.getId()));

          if (!Objects.equals(c.getFinca().getId(), fincaTarget.getId())) {
            throw new IllegalArgumentException("El cultivo no pertenece a la finca objetivo.");
          }

          List<String> cambiosC = new ArrayList<>();

          if (cu.getNombreCultivo() != null) {
            addChange(cambiosC, "nombreCultivo", c.getNombreCultivo(), cu.getNombreCultivo());
            c.setNombreCultivo(cu.getNombreCultivo());
          }
          if (cu.getVariedad() != null) {
            addChange(cambiosC, "variedad", c.getVariedad(), cu.getVariedad());
            c.setVariedad(cu.getVariedad());
          }
          if (cu.getArea() != null) {
            addChange(cambiosC, "area(ha)", fmtNum(c.getArea()), cu.getArea());
            c.setArea(BigDecimal.valueOf(cu.getArea()));
          }

          cultivoRepo.save(c);
          saveLog(userId, "cultivos", "UPDATE", c.getId(), "Actualizó → " + joinChanges(cambiosC));
        }
      }
    }

    // ---------------- CULTIVOS DELETE ----------------
    if (dto.getCultivosDeleteIds() != null) {
      for (Integer idDel : dto.getCultivosDeleteIds()) {
        Cultivo c = cultivoRepo.findById(idDel)
          .orElseThrow(() -> new NotFoundException("Cultivo no encontrado: id=" + idDel));

        if (!Objects.equals(c.getFinca().getId(), fincaTarget.getId())) {
          throw new IllegalArgumentException("El cultivo a eliminar no pertenece a la finca objetivo.");
        }

        cultivoRepo.deleteById(idDel);

        saveLog(userId, "cultivos", "DELETE", idDel,
          "Se eliminó cultivo id=" + idDel + ", nombre='" + nv(c.getNombreCultivo()) +
            "', variedad='" + nv(c.getVariedad()) + "', área=" + fmtNum(c.getArea()));
      }
    }

    return Map.of("ok", true);
  }

  @Override
  public void eliminarCultivo(Integer idCultivo, Integer userId) {
    Cultivo c = cultivoRepo.findById(idCultivo)
      .orElseThrow(() -> new NotFoundException("Cultivo no encontrado: id=" + idCultivo));

    cultivoRepo.deleteById(idCultivo);

    saveLog(userId, "cultivos", "DELETE", idCultivo,
      "Se eliminó cultivo id=" + idCultivo + ", nombre='" + nv(c.getNombreCultivo()) +
        "', variedad='" + nv(c.getVariedad()) + "', área=" + fmtNum(c.getArea()));
  }

  @Override
  public void eliminarProductorPorCedula(String cedula, Integer userId) {
    Productor prod = productorRepo.findByCedula(cedula)
      .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedula));

    List<Finca> fincas = fincaRepo.findByProductorId(prod.getId());
    for (Finca f : fincas) {
      List<Cultivo> cultivos = cultivoRepo.findByFincaId(f.getId());

      for (Cultivo c : cultivos) {
        saveLog(userId, "cultivos", "DELETE", c.getId(),
          "Se eliminó cultivo id=" + c.getId() + " (por borrado de productor " + nv(prod.getCedula()) + ")");
      }
      cultivoRepo.deleteAll(cultivos);

      saveLog(userId, "fincas", "DELETE", f.getId(),
        "Se eliminó finca id=" + f.getId() + " (por borrado de productor " + nv(prod.getCedula()) + ")");
    }

    fincaRepo.deleteAll(fincas);

    productorRepo.delete(prod);
    saveLog(userId, "productores", "DELETE", prod.getId(),
      "Se eliminó productor '" + nv(prod.getNombre()) + "' (cédula " + nv(prod.getCedula()) + ")");
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public RegistroResponseDto obtenerPorCedula(String cedula) {
    Productor prod = productorRepo.findByCedula(cedula)
      .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedula));

    List<Finca> fincas = fincaRepo.findByProductorId(prod.getId());
    Finca finca = fincas.isEmpty() ? null : fincas.get(0);

    RegistroResponseDto dto = new RegistroResponseDto();

    ProductorDto pd = new ProductorDto();
    pd.setCedula(prod.getCedula());
    pd.setNombre(prod.getNombre());
    pd.setTelefono(prod.getTelefono());
    pd.setGenero(prod.getGenero());
    pd.setPerteneceAsociacion(prod.isPerteneceAsociacion());
    pd.setNombreAsociacion(prod.getNombreAsociacion());
    dto.setProductor(pd);

    if (finca != null) {
      FincaDto fd = new FincaDto();
      fd.setAreaTotal(finca.getAreaTotal() == null ? null : finca.getAreaTotal().doubleValue());
      fd.setTipoActividad(finca.getTipoActividad());

      if (finca.getGeom() != null) {
        fd.setLon(finca.getGeom().getX());
        fd.setLat(finca.getGeom().getY());
      }

      fd.setVeredaCodigo(finca.getVereda() == null ? null : finca.getVereda().getCodigoCorto());
      fd.setGlobalid(finca.getGlobalid() == null ? null : finca.getGlobalid().toString());
      dto.setFinca(fd);
    }

    List<CultivoDto> cds = (finca == null)
      ? List.of()
      : cultivoRepo.findByFincaId(finca.getId()).stream()
        .map(c -> {
          CultivoDto x = new CultivoDto();
          x.setId(c.getId());
          x.setNombreCultivo(c.getNombreCultivo());
          x.setVariedad(c.getVariedad());
          x.setArea(c.getArea() == null ? null : c.getArea().doubleValue());
          return x;
        })
        .toList();

    dto.setCultivos(cds);

    return dto;
  }

  private void saveLog(Integer userId, String tabla, String op, Integer idAfectado, String detalle) {
    Log log = new Log();
    log.setFechaHora(LocalDateTime.now());
    log.setTablaAfectada(tabla);
    log.setOperacion(op);
    log.setIdRegistroAfectado(idAfectado);
    log.setDetalle(detalle);

    if (userId != null) {
      Usuario u = em.find(Usuario.class, userId);
      log.setUsuario(u);
      if (u != null) {
        log.setUsuarioNombre(u.getNombre());
        log.setUsuarioCorreo(u.getCorreo());
      }
    }
    logRepo.save(log);
  }

  private void saveLog(Integer userId, String tabla, String op, Integer idAfectado) {
    saveLog(userId, tabla, op, idAfectado, null);
  }

  private static String nv(Object o) {
    return o == null ? "" : String.valueOf(o);
  }

  private static String fmtNum(BigDecimal n) {
    return n == null ? "null" : n.stripTrailingZeros().toPlainString();
  }

  private static void addChange(List<String> out, String campo, Object antes, Object despues) {
    if (!Objects.equals(antes, despues)) {
      String a = String.valueOf(antes);
      String d = String.valueOf(despues);
      out.add(campo + ": '" + a + "' → '" + d + "'");
    }
  }

  private static String joinChanges(List<String> changes) {
    return (changes == null || changes.isEmpty())
      ? "(sin cambios detectados)"
      : String.join("; ", changes);
  }
}
