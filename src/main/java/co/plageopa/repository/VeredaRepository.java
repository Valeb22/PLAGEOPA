package co.plageopa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.plageopa.domain.Vereda;

public interface VeredaRepository extends JpaRepository<Vereda, Integer> {

    @Query(
      value = "SELECT * FROM veredas WHERE codigo_corto = :codigo ORDER BY id_vereda ASC LIMIT 1",
      nativeQuery = true
    )
    Optional<Vereda> findFirstByCodigoCorto(@Param("codigo") String codigo);
}
