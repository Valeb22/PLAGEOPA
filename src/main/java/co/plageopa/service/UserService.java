package co.plageopa.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import co.plageopa.domain.Usuario;
import co.plageopa.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    // ✅ Inyección por constructor explícito
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Usuario create(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        Usuario u = new Usuario();
        u.setNombre(username);
        u.setCorreo(email);
        u.setContraseña(rawPassword); // ⚠ solo para pruebas
        u.setRol("USER"); 
        return userRepository.save(u);
    }


    public Optional<Usuario> login(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getContraseña().equals(rawPassword));
    }
}
