package co.plageopa.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veredas")
@CrossOrigin(origins = {"http://localhost:4200"})
public class VeredaGeoController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public VeredaGeoController(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<JsonNode> getAllVeredasGeoJSON() throws Exception {
        final String sql = """
            SELECT json_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(json_agg(
                    json_build_object(
                        'type', 'Feature',
                        'geometry', ST_AsGeoJSON(geom)::json,
                        'properties', json_build_object(
                            'id', id,
                            'codigo_corto', codigo_corto
                        )
                    )
                ), '[]'::json)
            )::text AS geojson
            FROM veredas
            WHERE geom IS NOT NULL;
        """;

        List<String> rows = jdbc.queryForList(sql, String.class);
        String raw = rows.isEmpty()
                ? "{\"type\":\"FeatureCollection\",\"features\":[]}"
                : rows.get(0);

        // Parseamos a JSON real
        JsonNode node = mapper.readTree(raw);
        return ResponseEntity.ok(node);
    }

    @GetMapping(value = "/{codigoCorto}", produces = "application/json")
    public ResponseEntity<JsonNode> getVeredaByCodigoGeoJSON(@PathVariable String codigoCorto) throws Exception {
        final String sql = """
            SELECT json_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(json_agg(
                    json_build_object(
                        'type', 'Feature',
                        'geometry', ST_AsGeoJSON(geom)::json,
                        'properties', json_build_object(
                            'id', id,
                            'codigo_corto', codigo_corto
                        )
                    )
                ), '[]'::json)
            )::text AS geojson
            FROM veredas
            WHERE geom IS NOT NULL
              AND codigo_corto = ?;
        """;

        List<String> rows = jdbc.queryForList(sql, String.class, codigoCorto);
        String raw = rows.isEmpty()
                ? "{\"type\":\"FeatureCollection\",\"features\":[]}"
                : rows.get(0);

        JsonNode node = mapper.readTree(raw);
        return ResponseEntity.ok(node);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }
}
