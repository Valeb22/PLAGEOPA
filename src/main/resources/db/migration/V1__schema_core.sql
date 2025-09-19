-- Habilitar PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- =========================
-- 1) Tabla USUARIOS
-- =========================
DROP TABLE IF EXISTS usuarios CASCADE;
CREATE TABLE usuarios (
  id_usuario   SERIAL PRIMARY KEY,
  nombre       VARCHAR(200) NOT NULL,
  correo       VARCHAR(150) NOT NULL UNIQUE,
  contraseña   VARCHAR(255) NOT NULL,  
  rol          VARCHAR(20)  NOT NULL
);
CREATE INDEX usuarios_correo_idx ON usuarios (correo);

-- =========================
-- 2) Tabla LOGS
-- =========================
DROP TABLE IF EXISTS logs CASCADE;
CREATE TABLE logs (
  id_log                SERIAL PRIMARY KEY,
  id_usuario            INT REFERENCES usuarios(id_usuario), -- lo dejo sin NOT NULL por si borras usuarios
  fecha_hora            TIMESTAMP NOT NULL,
  tabla_afectada        VARCHAR(50) NOT NULL,
  operacion             VARCHAR(10) NOT NULL,
  id_registro_afectado  INT NOT NULL
);
CREATE INDEX logs_usuario_idx         ON logs (id_usuario);
CREATE INDEX logs_fecha_idx           ON logs (fecha_hora);
CREATE INDEX logs_tabla_registro_idx  ON logs (tabla_afectada, id_registro_afectado);

-- =========================
-- 3) Tabla PRODUCTORES
-- =========================
DROP TABLE IF EXISTS productores CASCADE;
CREATE TABLE productores (
  id_productor         SERIAL PRIMARY KEY,
  cedula               VARCHAR(10)  NOT NULL UNIQUE,
  nombre               VARCHAR(250) NOT NULL,
  telefono             VARCHAR(10),
  genero               VARCHAR(10)  NOT NULL,
  pertenece_asociacion BOOLEAN      NOT NULL DEFAULT FALSE,
  nombre_asociacion    VARCHAR(100),
  CONSTRAINT chk_genero_prod CHECK (genero IN ('Femenino','Masculino')),
  CONSTRAINT chk_cedula_digits   CHECK (cedula ~ '^[0-9]{1,10}$'),
  CONSTRAINT chk_telefono_digits CHECK (telefono IS NULL OR telefono ~ '^[0-9]{1,10}$')
);
CREATE INDEX productores_cedula_idx ON productores (cedula);
CREATE INDEX productores_nombre_idx ON productores (nombre);

-- =========================
-- 4) Tabla VEREDAS
-- =========================
DROP TABLE IF EXISTS veredas CASCADE;
CREATE TABLE veredas (
  id_vereda     SERIAL PRIMARY KEY,
  codigo_corto  VARCHAR(9)  NOT NULL,   -- p.ej. "254890001" (9 dígitos)
  geom          geometry(MULTIPOLYGON, 4326) NOT NULL
);
CREATE INDEX veredas_codigo_corto_idx ON veredas(codigo_corto);
CREATE INDEX veredas_geom_gix         ON veredas USING gist(geom);

-- =========================
-- 5) Tabla FINCAS
-- =========================
DROP TABLE IF EXISTS fincas CASCADE;
CREATE TABLE fincas (
    id_finca       SERIAL PRIMARY KEY,
    globalid       UUID UNIQUE NOT NULL,
    id_productor   INT NOT NULL REFERENCES productores(id_productor) ON DELETE CASCADE,
    id_vereda      INT REFERENCES veredas(id_vereda) ON DELETE SET NULL,  -- puede ser NULL
    area_total     NUMERIC(12,2) NOT NULL,
    tipo_actividad VARCHAR(200) NOT NULL,
    geom           geometry(POINT, 4326) NOT NULL,
    CONSTRAINT chk_area_finca_pos CHECK (area_total >= 0)
);
CREATE INDEX fincas_globalid_idx     ON fincas(globalid);
CREATE INDEX fincas_id_productor_idx ON fincas(id_productor);
CREATE INDEX fincas_id_vereda_idx    ON fincas(id_vereda);
CREATE INDEX fincas_geom_gix         ON fincas USING gist(geom);

-- =========================
-- 6) Tabla CULTIVOS
-- =========================
DROP TABLE IF EXISTS cultivos CASCADE;
CREATE TABLE cultivos (
  id_cultivos     SERIAL PRIMARY KEY,
  id_finca        INT NOT NULL,
  nombre_cultivo  VARCHAR(100) NOT NULL,
  variedad        VARCHAR(100),
  area            NUMERIC(12,2) NOT NULL,
  CONSTRAINT chk_area_cultivo_pos CHECK (area >= 0),
  CONSTRAINT fk_cultivos_finca
    FOREIGN KEY (id_finca) REFERENCES fincas(id_finca) ON DELETE CASCADE
);
CREATE INDEX cultivos_fk_finca_idx ON cultivos (id_finca);
