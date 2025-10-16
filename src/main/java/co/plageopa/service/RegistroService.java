package co.plageopa.service;

import co.plageopa.DTO.RegistroCreateDto;
import co.plageopa.DTO.RegistroResponseDto;
import co.plageopa.DTO.RegistroUpdateDto;

import java.util.Map;

public interface RegistroService {
  Map<String, Object> crearRegistro(RegistroCreateDto dto, Integer userId);
  Map<String, Object> actualizarPorCedula(String cedula, RegistroUpdateDto dto, Integer userId);
  void eliminarCultivo(Integer idCultivo, Integer userId);
  void eliminarProductorPorCedula(String cedula, Integer userId);
  RegistroResponseDto obtenerPorCedula(String cedula);

}