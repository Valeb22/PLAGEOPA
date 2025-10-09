package co.plageopa.repository;

import co.plageopa.domain.Vereda;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VeredaRepository extends JpaRepository<Vereda, Long> {
	Vereda findByCodigoCorto(String codigoCorto);
	List<Vereda> findAll();
}
