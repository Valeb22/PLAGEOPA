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
import org.springframework.stereotype.Service;

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

    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

    public RegistroServiceImpl(ProductorRepository productorRepo,
                               FincaRepository fincaRepo,
                               CultivoRepository cultivoRepo,
                               VeredaRepository veredaRepo,
                               LogRepository logRepo) {
        this.productorRepo = productorRepo;
        this.fincaRepo = fincaRepo;
        this.cultivoRepo = cultivoRepo;
        this.veredaRepo = veredaRepo;
        this.logRepo = logRepo;
    }

    // -----------------------
    // CREATE (alta integral)
    // -----------------------
    @Override
    public Map<String, Object> crearRegistro(RegistroCreateDto dto, Integer userId) {

        // 1) Validar cédula
        if (productorRepo.existsByCedula(dto.getProductor().getCedula())) {
            throw new IllegalArgumentException("La cédula ya existe: " + dto.getProductor().getCedula());
        }

        // 2) Validar punto único (lon/lat) para la finca
        double lon = dto.getFinca().getLon();
        double lat = dto.getFinca().getLat();
        if (fincaRepo.existsByLonLat(lon, lat)) {
            throw new IllegalArgumentException("Ya existe una finca en ese punto (lon/lat).");
        }

        // 3) Crear Productor
        Productor p = new Productor();
        p.setCedula(dto.getProductor().getCedula());
        p.setNombre(dto.getProductor().getNombre());
        p.setTelefono(dto.getProductor().getTelefono());
        p.setGenero(dto.getProductor().getGenero());
        p.setPerteneceAsociacion(dto.getProductor().isPerteneceAsociacion());
        p.setNombreAsociacion(dto.getProductor().getNombreAsociacion());
        p = productorRepo.save(p);
        saveLog(userId, "productores", "INSERT", p.getId());

        // 4) Crear Finca
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

        finca.setAreaTotal(BigDecimal.valueOf(f.getAreaTotal()));
        finca.setTipoActividad(f.getTipoActividad());

        Point pt = gf.createPoint(new Coordinate(lon, lat));
        pt.setSRID(4326);
        finca.setGeom(pt);

        finca = fincaRepo.save(finca);
        saveLog(userId, "fincas", "INSERT", finca.getId());

        // 5) Cultivos
        if (dto.getCultivos() != null) {
            for (CultivoDto c : dto.getCultivos()) {
                Cultivo cu = new Cultivo();
                cu.setFinca(finca);
                cu.setNombreCultivo(c.getNombreCultivo());
                cu.setVariedad(c.getVariedad());
                cu.setArea(BigDecimal.valueOf(c.getArea()));
                cu = cultivoRepo.save(cu);
                saveLog(userId, "cultivos", "INSERT", cu.getId());
            }
        }

        return Map.of(
                "ok", true,
                "productor_id", p.getId(),
                "finca_id", finca.getId(),
                "globalid", finca.getGlobalid().toString()
        );
    }

    // -----------------------
    // UPDATE por cédula
    // -----------------------
    @Override
    public Map<String, Object> actualizarPorCedula(String cedula, RegistroUpdateDto dto, Integer userId) {

        // 1) Productor
        Productor productor = productorRepo.findByCedula(cedula)
                .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedula));

        // 2) Actualizar solo campos permitidos
        if (dto.getProductor() != null) {
            ProductorUpdateDto pd = dto.getProductor();
            if (pd.getNombre() != null) productor.setNombre(pd.getNombre());
            if (pd.getTelefono() != null) productor.setTelefono(pd.getTelefono());
            if (pd.getPerteneceAsociacion() != null) productor.setPerteneceAsociacion(pd.getPerteneceAsociacion());
            if (pd.getNombreAsociacion() != null) productor.setNombreAsociacion(pd.getNombreAsociacion());
            productorRepo.save(productor);
            saveLog(userId, "productores", "UPDATE", productor.getId());
        }

        // 3) Finca a actualizar
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

        // 4) Actualizar finca (campos permitidos)
        if (dto.getFinca() != null) {
            FincaUpdateDto fu = dto.getFinca();

            if (fu.getLon() != null && fu.getLat() != null) {
                boolean existeOtro = fincaRepo.existsByLonLat(fu.getLon(), fu.getLat());
                boolean esMismo = fincaTarget.getGeom() != null &&
                        Double.compare(fincaTarget.getGeom().getX(), fu.getLon()) == 0 &&
                        Double.compare(fincaTarget.getGeom().getY(), fu.getLat()) == 0;
                if (existeOtro && !esMismo) {
                    throw new IllegalArgumentException("Ya existe otra finca registrada en ese punto (lon/lat).");
                }
                Point nuevo = gf.createPoint(new Coordinate(fu.getLon(), fu.getLat()));
                nuevo.setSRID(4326);
                fincaTarget.setGeom(nuevo);
            }

            if (fu.getAreaTotal() != null) fincaTarget.setAreaTotal(BigDecimal.valueOf(fu.getAreaTotal()));
            if (fu.getTipoActividad() != null) fincaTarget.setTipoActividad(fu.getTipoActividad());

            if (fu.getCodigoVereda() != null) {
                if (fu.getCodigoVereda().isBlank()) {
                    fincaTarget.setVereda(null);
                } else {
                	Vereda v = veredaRepo.findFirstByCodigoCorto(fu.getCodigoVereda())
                	        .orElseThrow(() -> new NotFoundException("Vereda no encontrada: " + fu.getCodigoVereda()));
                	fincaTarget.setVereda(v);


                }
            }

            fincaRepo.save(fincaTarget);
            saveLog(userId, "fincas", "UPDATE", fincaTarget.getId());
        }

        // 5) Cultivos: UPSERT
        if (dto.getCultivosUpsert() != null) {
            for (CultivoUpsertDto cu : dto.getCultivosUpsert()) {
                if (cu.getId() == null) {
                    // crear
                    Cultivo c = new Cultivo();
                    c.setFinca(fincaTarget);
                    c.setNombreCultivo(cu.getNombreCultivo());
                    c.setVariedad(cu.getVariedad());
                    c.setArea(BigDecimal.valueOf(cu.getArea()));
                    c = cultivoRepo.save(c);
                    saveLog(userId, "cultivos", "INSERT", c.getId());
                } else {
                    // editar
                    Cultivo c = cultivoRepo.findById(cu.getId())
                            .orElseThrow(() -> new NotFoundException("Cultivo no encontrado: id=" + cu.getId()));
                    if (!Objects.equals(c.getFinca().getId(), fincaTarget.getId())) {
                        throw new IllegalArgumentException("El cultivo no pertenece a la finca objetivo.");
                    }
                    if (cu.getNombreCultivo() != null) c.setNombreCultivo(cu.getNombreCultivo());
                    if (cu.getVariedad() != null) c.setVariedad(cu.getVariedad());
                    if (cu.getArea() != null) c.setArea(BigDecimal.valueOf(cu.getArea()));
                    cultivoRepo.save(c);
                    saveLog(userId, "cultivos", "UPDATE", c.getId());
                }
            }
        }

        // 6) Cultivos: DELETE por lista de IDs
        if (dto.getCultivosDeleteIds() != null) {
            for (Integer idDel : dto.getCultivosDeleteIds()) {
                Cultivo c = cultivoRepo.findById(idDel)
                        .orElseThrow(() -> new NotFoundException("Cultivo no encontrado: id=" + idDel));
                if (!Objects.equals(c.getFinca().getId(), fincaTarget.getId())) {
                    throw new IllegalArgumentException("El cultivo a eliminar no pertenece a la finca objetivo.");
                }
                cultivoRepo.deleteById(idDel);
                saveLog(userId, "cultivos", "DELETE", idDel);
            }
        }

        return Map.of("ok", true);
    }

    // -----------------------
    // DELETE cultivo (por id)
    // -----------------------
    @Override
    public void eliminarCultivo(Integer idCultivo, Integer userId) {
        Cultivo c = cultivoRepo.findById(idCultivo)
                .orElseThrow(() -> new NotFoundException("Cultivo no encontrado: id=" + idCultivo));
        cultivoRepo.deleteById(idCultivo);
        saveLog(userId, "cultivos", "DELETE", idCultivo);
    }

    // -----------------------
    // DELETE Productor (por cédula) + logs de TODO
    // -----------------------
    @Override
    public void eliminarProductorPorCedula(String cedula, Integer userId) {
        Productor prod = productorRepo.findByCedula(cedula)
            .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedula));

        // Trae fincas del productor
        List<Finca> fincas = fincaRepo.findByProductorId(prod.getId());
        for (Finca f : fincas) {
            // Log de cultivos ANTES
        	List<Cultivo> cultivos = cultivoRepo.findByFincaId(f.getId().longValue());
            for (Cultivo c : cultivos) {
                saveLog(userId, "cultivos", "DELETE", c.getId());
            }
            // Borra cultivos de la finca explícitamente
            cultivoRepo.deleteAll(cultivos);

            // Log de finca ANTES
            saveLog(userId, "fincas", "DELETE", f.getId());
        }

        // Borra fincas explícitamente
        fincaRepo.deleteAll(fincas);

        // Finalmente productor + log
        productorRepo.delete(prod);
        saveLog(userId, "productores", "DELETE", prod.getId());
    }

    // ---------- obtenerPorCedula ----------
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public RegistroResponseDto obtenerPorCedula(String cedula) {
        Productor prod = productorRepo.findByCedula(cedula)
            .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedula));

        List<Finca> fincas = fincaRepo.findByProductorId(prod.getId());
        Finca finca = fincas.isEmpty() ? null : fincas.get(0);

        RegistroResponseDto dto = new RegistroResponseDto();

        // productor
        ProductorDto pd = new ProductorDto();
        pd.setCedula(prod.getCedula());
        pd.setNombre(prod.getNombre());
        pd.setTelefono(prod.getTelefono());
        pd.setGenero(prod.getGenero());
        pd.setPerteneceAsociacion(prod.isPerteneceAsociacion());
        pd.setNombreAsociacion(prod.getNombreAsociacion());
        dto.setProductor(pd);

        // finca
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

        // cultivos (solo de esa finca)
        List<CultivoDto> cds = cultivoRepo.findByFincaId(finca == null ? -1L : finca.getId()).stream()
            .map(c -> {
                CultivoDto x = new CultivoDto();
                x.setId(c.getId());
                x.setNombreCultivo(c.getNombreCultivo());
                x.setVariedad(c.getVariedad());
                x.setArea(c.getArea() == null ? null : c.getArea().doubleValue());
                return x;
            }).toList();
        dto.setCultivos(cds);

        return dto;
    }

    // ---------- LOG Helper ----------
    private void saveLog(Integer userId, String tabla, String op, Integer idAfectado) {
        Log log = new Log();
        log.setFechaHora(LocalDateTime.now());
        log.setTablaAfectada(tabla);
        log.setOperacion(op);
        log.setIdRegistroAfectado(idAfectado);

        if (userId != null) {
            Usuario ref = em.getReference(Usuario.class, userId); // <-- el tipo de tu @Id en Usuario debe ser Integer
            log.setUsuario(ref);
        } else { log.setUsuario(null); }
        logRepo.save(log);
    }
}
