package co.plageopa.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.plageopa.domain.Usuario;
import co.plageopa.repository.UserRepository;
import co.plageopa.service.UserService;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Usuario create(String username, String email, String rawPassword, String rol) {
    String uName = username.trim();
    String mail = email.trim();

    if (userRepository.existsByNombre(uName)) {
      throw new IllegalArgumentException("El nombre de usuario ya existe");
    }
    if (userRepository.existsByCorreo(mail)) {
      throw new IllegalArgumentException("El correo ya está registrado");
    }

    Usuario u = new Usuario();
    u.setNombre(uName);
    u.setCorreo(mail);
    u.setContrasena(passwordEncoder.encode(rawPassword));
    u.setRol((rol == null || rol.isBlank()) ? "USER" : rol.trim().toUpperCase());

    // RECOMENDADO: obligar cambio si es clave temporal
    u.setMustChangePassword(true);

    return userRepository.save(u);
  }

  @Override
  public Optional<Usuario> login(String username, String rawPassword) {
	  return userRepository.findByNombre(username.trim())
	    .filter(u -> passwordEncoder.matches(rawPassword, u.getContrasena()));
	}


  @Override
  public void setPasswordAndForceChange(Integer userId, String rawPassword) {
    Usuario u = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

    u.setContrasena(passwordEncoder.encode(rawPassword));
    u.setMustChangePassword(true);
    userRepository.save(u);
  }
  @Override
  public void deleteUser(Integer userId) {
    if (userId == null) {
      throw new IllegalArgumentException("userId es obligatorio");
    }
    if (!userRepository.existsById(userId)) {
      throw new IllegalArgumentException("Usuario no existe");
    }
    userRepository.deleteById(userId);
  }
}
