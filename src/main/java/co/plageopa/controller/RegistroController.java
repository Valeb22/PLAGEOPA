package co.plageopa.controller;

import co.plageopa.DTO.RegistroCreateDto;
import co.plageopa.DTO.RegistroPageResponseDto;
import co.plageopa.DTO.RegistroResponseDto;
import co.plageopa.DTO.RegistroUpdateDto;
import co.plageopa.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/registro")
@CrossOrigin(
origins = "http://localhost:4200",
allowCredentials = "true",
allowedHeaders = {"Content-Type","X-User-Id","Authorization","Accept"},
methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class RegistroController {
private final RegistroService service;
public RegistroController(RegistroService service) { this.service = service; }

@GetMapping(value="/{cedula}", produces="application/json")
public RegistroResponseDto getByCedula(@PathVariable String cedula) {
 return service.obtenerPorCedula(cedula);
}

@PostMapping
public ResponseEntity<Map<String,Object>> crear(
   @Valid @RequestBody RegistroCreateDto dto,
   @RequestHeader(value="X-User-Id", required=false) Integer userId) {
 return ResponseEntity.ok(service.crearRegistro(dto, userId));
}

@PatchMapping("/{cedula}")
public ResponseEntity<Map<String,Object>> actualizar(
   @PathVariable String cedula,
   @Valid @RequestBody RegistroUpdateDto dto,
   @RequestHeader(value="X-User-Id", required=false) Integer userId) {
 return ResponseEntity.ok(service.actualizarPorCedula(cedula, dto, userId));
}

@DeleteMapping("/cultivos/{id}")
public ResponseEntity<Void> eliminarCultivo(
   @PathVariable Integer id,
   @RequestHeader(value="X-User-Id", required=false) Integer userId) {
 service.eliminarCultivo(id, userId);
 return ResponseEntity.noContent().build();
}

@DeleteMapping("/productores/{cedula}")
public ResponseEntity<Void> eliminarProductor(
   @PathVariable String cedula,
   @RequestHeader(value="X-User-Id", required=false) Integer userId) {
 service.eliminarProductorPorCedula(cedula, userId);
 return ResponseEntity.noContent().build();
}

@GetMapping("/list")
public RegistroPageResponseDto list(
    @RequestParam(defaultValue="0") int page,
    @RequestParam(defaultValue="100") int size,
    @RequestParam(defaultValue="") String q
){
  return service.listarRegistros(q, page, size);
}
}


