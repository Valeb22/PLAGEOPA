package co.plageopa.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.plageopa.domain.Usuario;
import co.plageopa.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")
public class UserController {

    private final UserService userService;

    // ✅ Inyección por constructor (sin Lombok)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() { return Map.of("ok", true, "message", "users api up"); }

    @GetMapping("/create")
    public ResponseEntity<?> createUser(@RequestParam String username,
                                        @RequestParam String email,
                                        @RequestParam String password) {
        try {
            Usuario u = userService.create(username, email, password);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Usuario creado",
                    "user", Map.of(
                            "id", u.getId(),
                            "username", u.getNombre(),
                            "email", u.getCorreo(),
                            "roles", u.getRol() == null ? Set.of() : u.getRol()
                    )
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "message", "Error interno"));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        Optional<Usuario> maybeUser = userService.login(username, password);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "message", "Credenciales inválidas"));
        }
        Usuario u = maybeUser.get();
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Login exitoso",
                "user", Map.of(
                        "id", u.getId(),
                        "username", u.getNombre(),
                        "email", u.getCorreo(),
                        "roles", u.getRol() == null ? Set.of() : u.getRol()
                )
        ));
    }
}
