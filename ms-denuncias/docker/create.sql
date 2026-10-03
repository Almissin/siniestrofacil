-- base de datos y tabla de denuncias de SiniestroFacil
CREATE DATABASE IF NOT EXISTS siniestrofacil;
USE siniestrofacil;

CREATE TABLE IF NOT EXISTS denuncias (
    folio            VARCHAR(20)  NOT NULL,
    patente          VARCHAR(6)   NOT NULL,
    rut_asegurado    VARCHAR(12)  NOT NULL,
    numero_poliza    VARCHAR(50)  NOT NULL,
    fecha_siniestro  DATE         NOT NULL,
    descripcion      VARCHAR(500) NOT NULL,
    estado           VARCHAR(20)  NOT NULL,
    fecha_registro   DATETIME(6)  NOT NULL,
    PRIMARY KEY (folio)
);
