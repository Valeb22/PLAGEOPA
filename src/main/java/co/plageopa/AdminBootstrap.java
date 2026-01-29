package co.plageopa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.plageopa.domain.Usuario;
import co.plageopa.repository.UserRepository;

@Configuration
public class AdminBootstrap {

  private static final String ADMIN_USERNAME = "Admin";
  private static final String ADMIN_EMAIL = "admin@plageopa.local";
  private static final String ADMIN_DEFAULT_PASSWORD = "admin123";

  @Bean
  CommandLineRunner seedAdmin(UserRepository userRepo, PasswordEncoder encoder) {
    return args -> {
      var adminOpt = userRepo.findByCorreo(ADMIN_EMAIL);

      if (adminOpt.isEmpty()) {
        Usuario u = new Usuario();
        u.setNombre(ADMIN_USERNAME);
        u.setCorreo(ADMIN_EMAIL);
        u.setRol("ADMIN");
        u.setContrasena(encoder.encode(ADMIN_DEFAULT_PASSWORD));
        u.setMustChangePassword(true); 
        userRepo.save(u);
        return;
      }

      Usuario u = adminOpt.get();
      String stored = u.getContrasena();

      if (stored == null || !stored.startsWith("$2a$") && !stored.startsWith("$2b$") && !stored.startsWith("$2y$")) {
        u.setContrasena(encoder.encode(ADMIN_DEFAULT_PASSWORD));
        u.setRol("ADMIN");
        u.setMustChangePassword(true);
        userRepo.save(u);
      }
    };
  }
}
