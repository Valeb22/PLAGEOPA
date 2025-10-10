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
    
    public String fincasFeatureCollectionByBbox(double minX, double minY, double maxX, double maxY) {
        String sql = """
            SELECT json_build_object(
              'type','FeatureCollection',
              'features', COALESCE(json_agg(
                json_build_object(
                  'type','Feature',
                  'geometry', ST_AsGeoJSON(f.geom)::json,
                  'properties', json_build_object(
                      'id_finca', f.id_finca,
                      'globalid', f.globalid,
                      'area_total', f.area_total,
                      'tipo_actividad', f.tipo_actividad,
                      'vereda_codigo', v.codigo_corto,
                      'productor_id', p.id_productor,
                      'productor_nombre', p.nombre,
                      'productor_cedula', p.cedula,
                      'productor_genero', p.genero,
                      'cultivos', COALESCE((
                        SELECT json_agg(json_build_object(
                            'id', c.id_cultivos,
                            'nombre', c.nombre_cultivo,
                            'variedad', c.variedad,
                            'area', c.area
                        ) ORDER BY c.id_cultivos)
                        FROM cultivos c
                        WHERE c.id_finca = f.id_finca
                      ), '[]'::json)
                  )
                )
              ), '[]'::json)
            )::text
            FROM fincas f
            JOIN productores p ON p.id_productor = f.id_productor
            LEFT JOIN veredas v ON v.id_vereda = f.id_vereda
            WHERE f.geom && ST_MakeEnvelope(?1, ?2, ?3, ?4, 4326);
        """;
        var q = em.createNativeQuery(sql);
        q.setParameter(1, minX); q.setParameter(2, minY);
        q.setParameter(3, maxX); q.setParameter(4, maxY);
        return (String) q.getSingleResult();
    }
    
    public String fincasFeatureCollection() {
        String sql = """
            SELECT json_build_object(
              'type','FeatureCollection',
              'features', COALESCE(json_agg(
                json_build_object(
                  'type','Feature',
                  'geometry', ST_AsGeoJSON(f.geom)::json,
                  'properties', json_build_object(
                      -- Finca
                      'id_finca', f.id_finca,
                      'globalid', f.globalid,
                      'area_total', f.area_total,
                      'tipo_actividad', f.tipo_actividad,
                      'lon', ST_X(f.geom),
                      'lat', ST_Y(f.geom),

                      -- Vereda (por FK si existe; si no, por relación espacial)
                      'vereda_codigo',
                        COALESCE(
                          v.codigo_corto,
                          (SELECT v2.codigo_corto
                             FROM veredas v2
                            WHERE ST_Intersects(v2.geom, f.geom)
                            LIMIT 1)
                        ),

                      -- Productor
                      'productor_id', p.id_productor,
                      'productor_cedula', p.cedula,
                      'productor_nombre', p.nombre,
                      'productor_telefono', p.telefono,
                      'productor_genero', p.genero,
                      'productor_pertenece_asociacion', p.pertenece_asociacion,
                      'productor_nombre_asociacion', NULLIF(p.nombre_asociacion, ''),

                      -- Cultivos de la finca (lista)
                      'cultivos', COALESCE((
                        SELECT json_agg(
                          json_build_object(
                            'id', c.id_cultivos,
                            'nombre', c.nombre_cultivo,
                            'variedad', NULLIF(c.variedad, ''),
                            'area', c.area
                          )
                          ORDER BY c.id_cultivos
                        )
                        FROM cultivos c
                        WHERE c.id_finca = f.id_finca
                      ), '[]'::json)
                  )
                )
              ), '[]'::json)
            )::text
            FROM fincas f
            JOIN productores p ON p.id_productor = f.id_productor
            LEFT JOIN veredas v ON v.id_vereda = f.id_vereda
            WHERE f.geom IS NOT NULL;
            """;
        return (String) em.createNativeQuery(sql).getSingleResult();
    }

}

