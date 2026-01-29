package co.plageopa.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.plageopa.domain.Log;

public interface LogRepository extends JpaRepository<Log, Integer> {

	@Query(value = """
			  SELECT
			    l.id_log               AS id_log,
			    l.fecha_hora           AS fecha_hora,
			    l.tabla_afectada       AS tabla_afectada,
			    l.operacion            AS operacion,
			    l.id_registro_afectado AS id_registro_afectado,
			    l.id_usuario           AS id_usuario,
			    COALESCE(u.nombre, l.usuario_nombre) AS usuario_nombre,
			    COALESCE(u.correo, l.usuario_correo) AS usuario_correo,
			    l.detalle              AS detalle
			  FROM logs l
			  LEFT JOIN usuarios u ON u.id_usuario = l.id_usuario
			  WHERE (l.fecha_hora)::date BETWEEN :fromDate AND :toDate
			  ORDER BY l.fecha_hora DESC
			""", nativeQuery = true)
			List<Map<String, Object>> findByRangeMap(@Param("fromDate") LocalDate fromDate,
			                                         @Param("toDate") LocalDate toDate);

    List<Log> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime from, LocalDateTime to);

}
