package co.plageopa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.plageopa.domain.Cultivo;
import co.plageopa.domain.Finca;

public interface CultivoRepository extends JpaRepository<Cultivo, Integer> {
	void deleteByFincaId(Integer fincaId);
	List<Cultivo> findByFincaId(Integer fincaId);
	List<Cultivo> findByFincaIdIn(List<Integer> fincaIds);
	@Query(value = """
			  SELECT * FROM fincas f
			  JOIN productores p ON p.id_productor = f.id_productor
			  WHERE p.cedula = ?1
			    AND ST_DWithin(f.geom, ST_SetSRID(ST_MakePoint(?2, ?3), 4326), CAST(0.000001 AS double precision))
			  LIMIT 1
			""", nativeQuery = true)
			Finca findOneByCedulaAndLonLat(String cedula, double lon, double lat);
	Optional<Cultivo> findFirstByFincaIdAndNombreCultivoIgnoreCaseAndVariedadIsNull(Integer fincaId, String nombreCultivo);

	  Optional<Cultivo> findFirstByFincaIdAndNombreCultivoIgnoreCaseAndVariedadIgnoreCase(Integer fincaId, String nombreCultivo, String variedad);
	}

