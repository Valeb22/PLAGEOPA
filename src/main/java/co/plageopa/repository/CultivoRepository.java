package co.plageopa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.plageopa.domain.Cultivo;

public interface CultivoRepository extends JpaRepository<Cultivo, Integer> {
	void deleteByFincaId(Long fincaId);
	List<Cultivo> findByFincaId(Long fincaId);

}

