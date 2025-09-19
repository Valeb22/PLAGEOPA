package co.plageopa.service.impl;

import co.plageopa.domain.Productor;
import co.plageopa.exception.NotFoundException;
import co.plageopa.repository.ProductorRepository;
import co.plageopa.service.ProductorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductorServiceImpl implements ProductorService {

    private final ProductorRepository repo;

    public ProductorServiceImpl(ProductorRepository repo) { this.repo = repo; }

    @Override
    @Transactional(readOnly = true)
    public List<Productor> listar() {
        return repo.findAll();
    }

    @Override
    public Productor crear(Productor p) {
        if (repo.existsByCedula(p.getCedula())) {
            throw new IllegalArgumentException("La cédula ya existe: " + p.getCedula());
        }
        return repo.save(p);
    }

    @Override
    public Productor actualizarPorCedula(String cedulaActual, Productor cambios) {
        Productor actual = repo.findByCedula(cedulaActual)
                .orElseThrow(() -> new NotFoundException("Productor no encontrado: cédula=" + cedulaActual));

        // Si cambia cédula, validar unicidad
        if (cambios.getCedula() != null && !cambios.getCedula().equals(actual.getCedula())) {
            if (repo.existsByCedula(cambios.getCedula())) {
                throw new IllegalArgumentException("La cédula ya existe: " + cambios.getCedula());
            }
            actual.setCedula(cambios.getCedula());
        }

        if (cambios.getNombre() != null) actual.setNombre(cambios.getNombre());
        if (cambios.getTelefono() != null) actual.setTelefono(cambios.getTelefono());
        if (cambios.getGenero() != null) actual.setGenero(cambios.getGenero());
        actual.setPerteneceAsociacion(cambios.isPerteneceAsociacion());
        if (cambios.getNombreAsociacion() != null) actual.setNombreAsociacion(cambios.getNombreAsociacion());

        return repo.save(actual);
    }

    @Override
    public void eliminarPorCedula(String cedula) {
        var opt = repo.findByCedula(cedula);
        if (opt.isEmpty()) throw new NotFoundException("Productor no encontrado: cédula=" + cedula);
        repo.deleteByCedula(cedula);
    }
}