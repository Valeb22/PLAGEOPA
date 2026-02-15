package co.plageopa.repository;

import co.plageopa.domain.Finca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FincaRepository extends JpaRepository<Finca, Integer> {

  Optional<Finca> findByGlobalid(UUID globalid);
  List<Finca> findByProductorId(Integer productorId);
  List<Finca> findByProductorIdIn(List<Integer> productorIds);

  @Query(
      value = "SELECT EXISTS (" +
              "  SELECT 1 FROM fincas " +
              "  WHERE ST_DWithin(geom, ST_SetSRID(ST_MakePoint(?1, ?2), 4326), CAST(0.000001 AS double precision))" +
              ")",
      nativeQuery = true
  )
  boolean existsByLonLat(double lon, double lat);

  // ✅ Para encontrar finca “natural key” cuando no hay globalid
  @Query(value = """
    SELECT f.*
    FROM fincas f
    JOIN productores p ON p.id_productor = f.id_productor
    WHERE p.cedula = :cedula
      AND ST_DWithin(
            f.geom,
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
            CAST(0.000001 AS double precision)
          )
    LIMIT 1
  """, nativeQuery = true)
  Optional<Finca> findOneByCedulaAndLonLat(
      @Param("cedula") String cedula,
      @Param("lon") double lon,
      @Param("lat") double lat
  );
}
