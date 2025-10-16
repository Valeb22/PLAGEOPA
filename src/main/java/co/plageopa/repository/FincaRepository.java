package co.plageopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.plageopa.domain.Finca;
import java.util.List;

public interface FincaRepository extends JpaRepository<Finca, Integer> {

  List<Finca> findByProductorId(Integer productorId);

  @Query(value = """
    select exists (
      select 1
      from fincas
      where ST_Equals(geom, ST_SetSRID(ST_MakePoint(?1, ?2), 4326))
    )
    """, nativeQuery = true)
  boolean existsByLonLat(double lon, double lat);
}
