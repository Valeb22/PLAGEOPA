package co.plageopa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    var repo = new HttpSessionSecurityContextRepository();

    http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .securityContext(sc -> sc.securityContextRepository(repo))
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
      .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) -> res.sendError(401, "No autenticado"))
        .accessDeniedHandler((req, res, ex) -> res.sendError(403, "Prohibido"))
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/users/login", "/api/users/ping").permitAll()
        .requestMatchers("/api/users").hasRole("ADMIN") // GET list
        .requestMatchers("/api/users/create", "/api/users/reset-password").hasRole("ADMIN")
        .requestMatchers("/api/users/change-password", "/api/users/logout").authenticated()
        .requestMatchers("/api/reports/**").authenticated()
        .anyRequest().authenticated()
      )
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable());

    return http.build();
  }
}
