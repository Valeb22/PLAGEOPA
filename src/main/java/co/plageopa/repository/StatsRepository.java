package co.plageopa.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StatsRepository {

  private final JdbcTemplate jdbc;

  public StatsRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // Ajusta nombres de tablas/columnas a los tuyos reales:
  public long countProductores() {
    return jdbc.queryForObject("select count(*) from productores", Long.class);
  }

  public long countFincas() {
    return jdbc.queryForObject("select count(*) from fincas", Long.class);
  }

  public long countCultivos() {
    return jdbc.queryForObject("select count(*) from cultivos", Long.class);
  }

  public double sumAreaTotalFincas() {
    Double v = jdbc.queryForObject("select coalesce(sum(area_total),0) from fincas", Double.class);
    return v == null ? 0.0 : v;
  }

  public List<RowKVDouble> topAreaPorCultivo(int limit) {
    String sql = """
      select c.nombre_cultivo as k, coalesce(sum(c.area),0) as v
      from cultivos c
      group by c.nombre_cultivo
      order by v desc
      limit ?
    """;
    return jdbc.query(sql, (rs, i) -> new RowKVDouble(rs.getString("k"), rs.getDouble("v")), limit);
  }

  public List<RowKVLong> topFincasPorCultivo(int limit) {
    String sql = """
      select c.nombre_cultivo as k, count(distinct c.id_finca) as v
      from cultivos c
      group by c.nombre_cultivo
      order by v desc
      limit ?
    """;
    return jdbc.query(sql, (rs, i) -> new RowKVLong(rs.getString("k"), rs.getLong("v")), limit);
  }

  public List<RowKVLong> topFincasPorVereda(int limit) {
    String sql = """
      select f.vereda_codigo as k, count(*) as v
      from fincas f
      group by f.vereda_codigo
      order by v desc
      limit ?
    """;
    return jdbc.query(sql, (rs, i) -> new RowKVLong(rs.getString("k"), rs.getLong("v")), limit);
  }

  public List<RowKVDouble> topAreaPorVereda(int limit) {
    String sql = """
      select f.vereda_codigo as k, coalesce(sum(f.area_total),0) as v
      from fincas f
      group by f.vereda_codigo
      order by v desc
      limit ?
    """;
    return jdbc.query(sql, (rs, i) -> new RowKVDouble(rs.getString("k"), rs.getDouble("v")), limit);
  }

  public List<RowKVLong> generoConteo() {
    String sql = """
      select p.genero as k, count(*) as v
      from productores p
      group by p.genero
      order by v desc
    """;
    return jdbc.query(sql, (rs, i) -> new RowKVLong(rs.getString("k"), rs.getLong("v")));
  }

  public List<RowKVLong> asociacionConteo() {
    String sql = """
      select case when p.pertenece_asociacion then 'Sí' else 'No' end as k, count(*) as v
      from productores p
      group by k
      order by v desc
    """;
    return jdbc.query(sql, (rs, i) -> new RowKVLong(rs.getString("k"), rs.getLong("v")));
  }

  public List<TopProdRow> topProductores(int limit) {
    String sql = """
      select p.cedula as cedula, p.nombre as nombre,
             coalesce(sum(f.area_total),0) as area,
             count(f.id_finca) as fincas
      from productores p
      join fincas f on f.productor_cedula = p.cedula
      group by p.cedula, p.nombre
      order by area desc
      limit ?
    """;
    return jdbc.query(sql, (rs, i) -> new TopProdRow(
      rs.getString("cedula"),
      rs.getString("nombre"),
      rs.getDouble("area"),
      rs.getLong("fincas")
    ), limit);
  }

  public record RowKVDouble(String k, double v) {}
  public record RowKVLong(String k, long v) {}
  public record TopProdRow(String cedula, String nombre, double area, long fincas) {}
}
