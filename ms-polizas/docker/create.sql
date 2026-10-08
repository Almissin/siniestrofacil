-- crea la base de datos propia de ms-polizas dentro del contenedor mysql-siniestrofacil
-- y da permisos al usuario de la aplicacion (sfuser)
CREATE DATABASE IF NOT EXISTS siniestrofacil_polizas;
GRANT ALL PRIVILEGES ON siniestrofacil_polizas.* TO 'sfuser'@'%';
FLUSH PRIVILEGES;

USE siniestrofacil_polizas;

-- polizas de seguro automotriz
CREATE TABLE IF NOT EXISTS polizas (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero         VARCHAR(20) NOT NULL UNIQUE,
    rut_asegurado  VARCHAR(12) NOT NULL,
    patente        VARCHAR(6)  NOT NULL,
    fecha_inicio   DATE        NOT NULL,
    fecha_termino  DATE        NOT NULL,
    activa         BOOLEAN     NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6) NOT NULL
);

-- coberturas: cada cobertura pertenece a una poliza (relacion N:1)
CREATE TABLE IF NOT EXISTS coberturas (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL,
    monto_maximo BIGINT      NOT NULL,
    poliza_id    BIGINT      NOT NULL,
    CONSTRAINT fk_coberturas_poliza FOREIGN KEY (poliza_id) REFERENCES polizas (id) ON DELETE CASCADE
);
