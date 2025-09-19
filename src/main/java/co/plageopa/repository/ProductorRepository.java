package co.plageopa.repository;

import co.plageopa.domain.Productor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductorRepository extends JpaRepository<Productor, Integer> {
    Optional<Productor> findByCedula(String cedula);
    boolean existsByCedula(String cedula);
    void deleteByCedula(String cedula);
    boolean existsByCedulaAndIdNot(String cedula, Integer id); // para actualizar cambiando cédula
}
