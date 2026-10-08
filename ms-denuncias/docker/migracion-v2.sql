-- migracion de ms-denuncias v1 -> v2 sobre la base existente "siniestrofacil"
-- se ejecuta UNA vez, con el contenedor mysql-siniestrofacil corriendo y ANTES de levantar la v2.
USE siniestrofacil;

-- "estado" como texto para aceptar los nuevos valores:
-- RECIBIDA, RECHAZADA, TALLER_ASIGNADO y VEHICULO_RECIBIDO
ALTER TABLE denuncias MODIFY estado VARCHAR(20) NOT NULL;

-- las columnas nuevas (taller_id, nombre_taller, motivo_rechazo, fecha_resultado, fecha_recepcion)
-- y la tabla fotografias las crea Hibernate automaticamente al iniciar (spring.jpa.hibernate.ddl-auto=update)
