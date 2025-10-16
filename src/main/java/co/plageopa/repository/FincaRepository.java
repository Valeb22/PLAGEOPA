package co.plageopa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.plageopa.domain.Finca;
import java.util.List;

public interface FincaRepository extends JpaRepository<Finca, Integer> {

  List<Finca> findByProductorId(Integer productorId);

  @Query(
		  value = "SELECT EXISTS (" +
		          "  SELECT 1 FROM fincas " +
		          "  WHERE ST_DWithin(geom, ST_SetSRID(ST_MakePoint(?1, ?2), 4326), CAST(0.000001 AS double precision))" +
		          ")",
		  nativeQuery = true
		)
		boolean existsByLonLat(double lon, double lat);

}
