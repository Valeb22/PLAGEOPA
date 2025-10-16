package co.plageopa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.plageopa.domain.Usuario;
public interface UserRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombre(String nombre);
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByNombre(String nombre);
    boolean existsByCorreo(String correo);
}
