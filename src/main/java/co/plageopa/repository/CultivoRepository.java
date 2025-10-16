package co.plageopa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.plageopa.domain.Cultivo;

public interface CultivoRepository extends JpaRepository<Cultivo, Integer> {
	void deleteByFincaId(Integer fincaId);
	List<Cultivo> findByFincaId(Integer fincaId);

}

