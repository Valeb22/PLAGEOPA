package co.plageopa.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.plageopa.domain.Log;

public interface LogRepository extends JpaRepository<Log, Integer> {
	
	  @Query(value = """
	    SELECT 
	      l.id_log             AS id_log,
	      l.fecha_hora         AS fecha_hora,
	      l.tabla_afectada     AS tabla,
	      l.operacion          AS operacion,
	      l.id_registro_afectado AS id_afectado,
	      l.id_usuario         AS usuario_id,
	      u.nombre             AS usuario_nombre,
	      u.correo             AS usuario_correo
	    FROM logs l
	    LEFT JOIN usuarios u ON u.id_usuario = l.id_usuario
	    ORDER BY l.fecha_hora DESC
	    LIMIT :limit
	  """, nativeQuery = true)
	  List<Map<String,Object>> findLastLogs(int limit);

}
