package co.plageopa.service;

import co.plageopa.domain.Productor;

import java.util.List;

public interface ProductorService {
    List<Productor> listar();
    Productor crear(Productor p);
    Productor actualizarPorCedula(String cedulaActual, Productor cambios);
    void eliminarPorCedula(String cedula);
}
