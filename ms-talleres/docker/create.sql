-- crea la base de datos propia de ms-talleres dentro del contenedor mysql-siniestrofacil
-- y da permisos al usuario de la aplicacion (sfuser)
CREATE DATABASE IF NOT EXISTS siniestrofacil_talleres;
GRANT ALL PRIVILEGES ON siniestrofacil_talleres.* TO 'sfuser'@'%';
FLUSH PRIVILEGES;

USE siniestrofacil_talleres;

-- talleres en convenio
CREATE TABLE IF NOT EXISTS talleres (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL UNIQUE,
    rut            VARCHAR(12)  NOT NULL UNIQUE,
    direccion      VARCHAR(150) NOT NULL,
    comuna         VARCHAR(60)  NOT NULL,
    telefono       VARCHAR(20),
    email          VARCHAR(120),
    cupo_maximo    INT          NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6)  NOT NULL
);

-- asignaciones: cada asignacion pertenece a un taller (relacion N:1)
CREATE TABLE IF NOT EXISTS asignaciones (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio_denuncia     VARCHAR(20) NOT NULL UNIQUE,
    estado             VARCHAR(20) NOT NULL,
    fecha_asignacion   DATETIME(6) NOT NULL,
    fecha_finalizacion DATETIME(6),
    taller_id          BIGINT      NOT NULL,
    CONSTRAINT fk_asignaciones_taller FOREIGN KEY (taller_id) REFERENCES talleres (id)
);
