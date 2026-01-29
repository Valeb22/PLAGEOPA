package co.plageopa.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import co.plageopa.DTO.CreateUserRequest;
import co.plageopa.DTO.LoginRequest;
import co.plageopa.DTO.ResetPasswordRequest;
import co.plageopa.DTO.ChangePasswordRequest;
import co.plageopa.domain.Usuario;
import co.plageopa.repository.UserRepository;
import co.plageopa.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

  private final UserService userService;
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;

  public UserController(UserService userService, UserRepository userRepo, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/ping")
  public Map<String, Object> ping() {
    return Map.of("ok", true, "message", "users api up");
  }

  @PostMapping("/create")
  public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
    if (req == null || req.username == null || req.username.isBlank()
        || req.email == null || req.email.isBlank()
        || req.password == null || req.password.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "username, email y password son obligatorios"));
    }

    Usuario u = userService.create(req.username, req.email, req.password, req.rol);

    return ResponseEntity.ok(Map.of(
      "ok", true,
      "message", "Usuario creado",
      "user", Map.of(
        "id", u.getId(),
        "username", u.getNombre(),
        "email", u.getCorreo(),
        "roles", u.getRol() == null ? Set.of() : u.getRol(),
        "mustChangePassword", Boolean.TRUE.equals(u.getMustChangePassword())
      )
    ));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
    if (req == null || req.username == null || req.username.isBlank()
        || req.password == null || req.password.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Username y password son obligatorios"));
    }

    var maybe = userService.login(req.username, req.password);
    if (maybe.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "message", "Usuario/contraseña inválidos"));
    }

    Usuario u = maybe.get();

    var role = (u.getRol() == null || u.getRol().isBlank()) ? "USER" : u.getRol().toUpperCase();
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

    var auth = new UsernamePasswordAuthenticationToken(u.getNombre(), null, authorities);

    var ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(auth);
    SecurityContextHolder.setContext(ctx);

    var session = request.getSession(true);
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);

    return ResponseEntity.ok(Map.of(
      "ok", true,
      "message", "Login exitoso",
      "user", Map.of(
        "id", u.getId(),
        "username", u.getNombre(),
        "email", u.getCorreo(),
        "rol", role,
        "mustChangePassword", Boolean.TRUE.equals(u.getMustChangePassword())
      )
    ));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    var session = request.getSession(false);
    if (session != null) session.invalidate();
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(Map.of("ok", true, "message", "Logout OK"));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
    if (req == null || req.userId == null || req.newPassword == null || req.newPassword.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId y newPassword son obligatorios"));
    }
    userService.setPasswordAndForceChange(req.userId, req.newPassword);
    return ResponseEntity.ok(Map.of("ok", true, "message", "Password reseteado. Usuario debe cambiarla al ingresar."));
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {

    if (req == null || req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()
        || req.getNewPassword() == null || req.getNewPassword().isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Datos incompletos"));
    }

    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "message", "No autenticado"));
    }

    String username = String.valueOf(auth.getPrincipal());

    Usuario u = userRepo.findByNombre(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));

    if (!passwordEncoder.matches(req.getCurrentPassword(), u.getContrasena())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("ok", false, "message", "Contraseña actual inválida"));
    }

    u.setContrasena(passwordEncoder.encode(req.getNewPassword()));
    u.setMustChangePassword(false);
    userRepo.save(u);

    return ResponseEntity.ok(Map.of("ok", true, "message", "Contraseña actualizada"));
  }
  @GetMapping
  public ResponseEntity<?> listUsers() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("ok", false, "message", "No autenticado"));
    }

    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("ok", false, "message", "Solo ADMIN"));
    }

    var users = userRepo.findAll().stream().map(u -> Map.of(
        "id", u.getId(),
        "username", u.getNombre(),
        "email", u.getCorreo(),
        "rol", (u.getRol() == null ? "USER" : u.getRol().toUpperCase()),
        "mustChangePassword", Boolean.TRUE.equals(u.getMustChangePassword())
    )).toList();

    return ResponseEntity.ok(users);
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("ok", false, "message", "No autenticado"));
    }

    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    if (!isAdmin) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("ok", false, "message", "Solo ADMIN puede eliminar usuarios"));
    }

    try {
      userService.deleteUser(id);
      return ResponseEntity.ok(Map.of("ok", true, "message", "Usuario eliminado"));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "message", ex.getMessage()));
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("ok", false, "message", "No se puede eliminar: el usuario tiene registros asociados."));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("ok", false, "message", "Error eliminando usuario"));
    }
  }
}
