package co.plageopa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class GeoDao {

    @PersistenceContext
    private EntityManager em;

    public String veredasFeatureCollection() {
        String sql = """
            SELECT json_build_object(
              'type',       'FeatureCollection',
              'features',   COALESCE(json_agg(
                               json_build_object(
                                 'type',       'Feature',
                                 'geometry',   ST_AsGeoJSON(v.geom)::json,
                                 'properties', json_build_object(
                                     'id_vereda', v.id_vereda,
                                     'codigo_corto', v.codigo_corto
                                 )
                               )
                             ), '[]'::json)
            )::text
            FROM veredas v
            """;
        Query q = em.createNativeQuery(sql);
        return (String) q.getSingleResult();
    }

    public String veredasFeatureCollectionByBbox(double minX, double minY, double maxX, double maxY) {
        String sql = """
            SELECT json_build_object(
              'type','FeatureCollection',
              'features', COALESCE(json_agg(
                 json_build_object(
                   'type','Feature',
                   'geometry', ST_AsGeoJSON(v.geom)::json,
                   'properties', json_build_object(
                       'id_vereda', v.id_vereda,
                       'codigo_corto', v.codigo_corto
                   )
                 )
              ), '[]'::json)
            )::text
            FROM veredas v
            WHERE v.geom && ST_MakeEnvelope(?1, ?2, ?3, ?4, 4326)
            """;
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, minX);
        q.setParameter(2, minY);
        q.setParameter(3, maxX);
        q.setParameter(4, maxY);
        return (String) q.getSingleResult();
    }
}

