package co.plageopa.service;

import co.plageopa.DTO.StatsReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class StatsService {

  private final JdbcTemplate jdbc;

  public StatsService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public StatsReport getStats() {

    long totalProductores = qLong("SELECT COUNT(*) FROM productores");
    long totalFincas      = qLong("SELECT COUNT(*) FROM fincas");
    long totalCultivos    = qLong("SELECT COUNT(*) FROM cultivos");

    long cultivosUnicos   = qLong("SELECT COUNT(DISTINCT UPPER(TRIM(nombre_cultivo))) FROM cultivos");

    double areaTotalFincas = qDouble("SELECT COALESCE(SUM(area_total),0) FROM fincas");
    double areaTotalCultivada = qDouble("SELECT COALESCE(SUM(area),0) FROM cultivos");

    double usoSueloPct = 0.0;
    if (areaTotalFincas > 0) {
      usoSueloPct = (areaTotalCultivada / areaTotalFincas) * 100.0;
    }

    double areaPromedioPorFinca = 0.0;
    if (totalFincas > 0) {
      areaPromedioPorFinca = areaTotalFincas / (double) totalFincas;
    }

    // Percentiles de área de finca (PostgreSQL)
    double p25 = qDouble("""
      SELECT COALESCE(
        percentile_cont(0.25) WITHIN GROUP (ORDER BY area_total), 0
      ) FROM fincas
    """);
    double mediana = qDouble("""
      SELECT COALESCE(
        percentile_cont(0.50) WITHIN GROUP (ORDER BY area_total), 0
      ) FROM fincas
    """);
    double p75 = qDouble("""
      SELECT COALESCE(
        percentile_cont(0.75) WITHIN GROUP (ORDER BY area_total), 0
      ) FROM fincas
    """);

    // Top cultivos por área
    LinkedHashMap<String, Double> topCultivosArea = qTopDouble("""
      SELECT UPPER(TRIM(nombre_cultivo)) AS k, COALESCE(SUM(area),0) AS v
      FROM cultivos
      GROUP BY UPPER(TRIM(nombre_cultivo))
      ORDER BY v DESC
      LIMIT 10
    """);

    // Top cultivos por #fincas (distintas)
    LinkedHashMap<String, Long> topCultivosFincas = qTopLong("""
      SELECT UPPER(TRIM(nombre_cultivo)) AS k, COUNT(DISTINCT id_finca) AS v
      FROM cultivos
      GROUP BY UPPER(TRIM(nombre_cultivo))
      ORDER BY v DESC
      LIMIT 10
    """);

    // Distribución por género (productores)
    LinkedHashMap<String, Long> generoProductores = qTopLong("""
      SELECT COALESCE(genero,'NO DEFINIDO') AS k, COUNT(*) AS v
      FROM productores
      GROUP BY COALESCE(genero,'NO DEFINIDO')
      ORDER BY v DESC
    """);

    // Asociaciones (Sí/No)
    LinkedHashMap<String, Long> asociacionProductores = qTopLong("""
      SELECT CASE WHEN pertenece_asociacion THEN 'Sí' ELSE 'No' END AS k,
             COUNT(*) AS v
      FROM productores
      GROUP BY CASE WHEN pertenece_asociacion THEN 'Sí' ELSE 'No' END
      ORDER BY v DESC
    """);

    return new StatsReport(
      totalProductores,
      totalFincas,
      totalCultivos,
      cultivosUnicos,

      areaTotalFincas,
      areaTotalCultivada,
      usoSueloPct,
      areaPromedioPorFinca,

      p25,
      mediana,
      p75,

      topCultivosArea,
      topCultivosFincas,

      generoProductores,
      asociacionProductores
    );
  }

  // =========================
  // Helpers JDBC (SIN ambigüedad)
  // =========================

  private long qLong(String sql) {
    Long v = jdbc.queryForObject(sql, Long.class);
    return v == null ? 0L : v;
  }

  private double qDouble(String sql) {
    Double v = jdbc.queryForObject(sql, Double.class);
    return v == null ? 0.0 : v;
  }

  private LinkedHashMap<String, Double> qTopDouble(String sql) {
    LinkedHashMap<String, Double> map = new LinkedHashMap<>();
    jdbc.query(sql, (rs) -> {
      map.put(rs.getString("k"), rs.getDouble("v"));
    });
    return map;
  }

  private LinkedHashMap<String, Long> qTopLong(String sql) {
    LinkedHashMap<String, Long> map = new LinkedHashMap<>();
    jdbc.query(sql, (rs) -> {
      map.put(rs.getString("k"), rs.getLong("v"));
    });
    return map;
  }
}
