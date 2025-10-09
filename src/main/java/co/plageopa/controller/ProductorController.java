package co.plageopa.controller;

import co.plageopa.domain.Productor;
import co.plageopa.service.ProductorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productores")
public class ProductorController {

    private final ProductorService service;
    public ProductorController(ProductorService service) { this.service = service; }

    // LISTAR TODOS
    @GetMapping
    public List<Productor> listar() {
        return service.listar();
    }

    // CREAR
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Productor crear(@Valid @RequestBody Productor p) {
        return service.crear(p);
    }

    // ACTUALIZAR (por cédula de la URL, permite cambiarla en el body si se desea)
    @PatchMapping("/{cedula}")
    public Productor actualizar(@PathVariable String cedula, @RequestBody Productor cambios) {
        return service.actualizarPorCedula(cedula, cambios);
    }

    // ELIMINAR (por cédula)
    @DeleteMapping("/{cedula}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String cedula) {
        service.eliminarPorCedula(cedula);
    }
}