-- crea la base de datos propia de ms-usuarios dentro del contenedor mysql-siniestrofacil
-- y da permisos al usuario de la aplicacion (sfuser)
CREATE DATABASE IF NOT EXISTS siniestrofacil_usuarios;
GRANT ALL PRIVILEGES ON siniestrofacil_usuarios.* TO 'sfuser'@'%';
FLUSH PRIVILEGES;

USE siniestrofacil_usuarios;

-- roles del sistema (ADMIN, ASEGURADO, TALLER)
CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(20)  NOT NULL UNIQUE,
    descripcion VARCHAR(150)
);

-- usuarios: cada usuario pertenece a un rol (relacion N:1)
CREATE TABLE IF NOT EXISTS usuarios (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    rut            VARCHAR(12)  NOT NULL UNIQUE,
    nombre         VARCHAR(80)  NOT NULL,
    apellido       VARCHAR(80)  NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    password       VARCHAR(100) NOT NULL,
    telefono       VARCHAR(20),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    taller_id      BIGINT,
    rol_id         BIGINT       NOT NULL,
    fecha_creacion DATETIME(6)  NOT NULL,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
);
