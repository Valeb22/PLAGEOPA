package co.plageopa.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import co.plageopa.domain.Productor;

public interface ProductorRepository extends JpaRepository<Productor, Integer> { 
	Optional<Productor> findByCedula(String cedula); 
	boolean existsByCedula(String cedula); 
	void deleteByCedula(String cedula);
	boolean existsByCedulaAndIdNot(String cedula, Integer id);
	  Page<Productor> findAll(Pageable pageable);
	  Page<Productor> findByCedulaContainingIgnoreCaseOrNombreContainingIgnoreCase(
	      String cedula, String nombre, Pageable pageable
	  );
}
