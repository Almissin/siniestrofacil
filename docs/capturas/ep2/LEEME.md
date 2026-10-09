# Capturas de la EP2 — qué tomar y cómo

Guarda cada captura en **esta carpeta** con **el nombre exacto** de la tabla. Así aparecen solas en la sección *Evidencias* del README principal.

**Cómo tomar la captura en Windows:** `Win + Shift + S` → seleccionar el área → abrir la notificación → **Guardar como** → formato **PNG**.

> **Antes de empezar:** MySQL encendido, las bases creadas y los 4 microservicios levantados (Pasos 2 a 5 del README). Para las capturas 11 y 12, ejecuta antes la colección `ms-denuncias.postman_collection.json` completa, así habrá asignaciones y denuncias en distintos estados.

| # | Nombre del archivo | Qué mostrar | Cómo obtenerlo |
|---|---|---|---|
| 01 | `01-docker-mysql-up.png` | `mysql-siniestrofacil` con estado **Up** y `0.0.0.0:3306->3306/tcp` | `docker ps` |
| 02 | `02-mysql-bases.png` | Las 4 bases de datos | `docker exec -it mysql-siniestrofacil mysql -usfuser -psfpass -e "SHOW DATABASES;"` |
| 03 | `03-maven-build-success.png` | `Tests run: ... Failures: 0`, `BUILD SUCCESS` y la ruta del `.jar` | En un microservicio: `.\mvnw.cmd clean package` |
| 04 | `04-vscode-4-microservicios.png` | VS Code con los 4 servicios corriendo (lista desplegable de la barra de depuración o las 4 terminales con `Tomcat started on port ...`) | F5 con **"SiniestroFacil: levantar todo"** |
| 05 | `05-swagger-usuarios-login.png` | `POST /auth/login` con respuesta **200** y el `token` | http://localhost:8080/ → Try it out → Execute |
| 06 | `06-swagger-denuncias-201.png` | `POST /denuncias` con respuesta **201**, el `folio` y estado `RECIBIDA` | http://localhost:8081/ → Authorize con el token del **asegurado** → Try it out → Execute |
| 07 | `07-swagger-polizas-vigencia.png` | `GET /polizas/POL-002/vigencia` con `"vigente": false` y el motivo | http://localhost:8082/ → Authorize con el token del **admin** |
| 08 | `08-swagger-talleres-asignacion.png` | `POST /asignaciones` con respuesta **201** y `nombreTaller` | http://localhost:8083/ → Authorize con el token del **admin** → usar el folio de la captura 06 |
| 09 | `09-sql-usuarios-roles.png` | Usuarios con su rol | Consulta de **ms-usuarios** del Paso 7 del README |
| 10 | `10-sql-polizas-coberturas.png` | Pólizas con sus coberturas | Consulta de **ms-polizas** del Paso 7 |
| 11 | `11-sql-talleres-asignaciones.png` | Talleres con folios asignados | Consulta de **ms-talleres** del Paso 7 |
| 12 | `12-sql-denuncias-estados.png` | Denuncias en `RECHAZADA`, `TALLER_ASIGNADO` y `VEHICULO_RECIBIDO`, con fotos | Consulta de **ms-denuncias** del Paso 7 |
| 13 | `13-postman-runner-denuncias.png` | Resultado del Collection Runner con todas las pruebas en verde | Postman → `ms-denuncias - pruebas` → **Run collection** |

**Consejos para que se vean bien:**
- En Swagger, baja hasta que se vean juntos el **código de respuesta** y el **cuerpo JSON**.
- En la terminal, agranda la ventana para que las tablas de MySQL no se corten.
- Si una consulta muestra muchas filas, basta con que se vean las primeras.
