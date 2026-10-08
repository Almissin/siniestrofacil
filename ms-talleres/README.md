# 🔧 SiniestroFácil — Microservicio `ms-talleres`

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Contenedores-2496ED?logo=docker&logoColor=white)

Microservicio REST para **gestionar el listado de talleres en convenio** y **asignar un taller disponible** a una denuncia cuya póliza está vigente. Cada taller tiene un **cupo máximo** de vehículos; el servicio elige el taller activo con más cupos libres. Si ningún taller tiene cupo, responde `409` y la denuncia queda pendiente para procesarse más tarde.

> Proyecto académico de la asignatura **JVY0101 – Java: Diseño y Construcción de Soluciones Nativas en Nube**, DUOC UC.

---

## 📑 Tabla de contenidos

1. [Tecnologías](#-tecnologías)
2. [Rol en SiniestroFácil](#-rol-en-siniestrofácil)
3. [Estructura del proyecto](#-estructura-del-proyecto)
4. [Base de datos MySQL en Docker](#-base-de-datos-mysql-en-docker)
5. [Ejecutar el microservicio](#-ejecutar-el-microservicio)
6. [Microservicio en Docker](#-microservicio-en-docker)
7. [Endpoints de la API](#-endpoints-de-la-api)
8. [Validaciones y reglas de negocio](#-validaciones-y-reglas-de-negocio)
9. [Manejo de errores](#-manejo-de-errores)
10. [Documentación con Swagger](#-documentación-con-swagger-openapi)
11. [Pruebas con Postman](#-pruebas-con-postman)
12. [Pruebas con Maven](#-pruebas-con-maven)
13. [Checklist de revisión](#-checklist-de-revisión)
14. [Solución de problemas](#-solución-de-problemas)

---

## 🛠 Tecnologías

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java (OpenJDK) | 25 | Lenguaje del microservicio |
| Spring Boot | 4.1.1 | Framework base |
| `spring-boot-starter-webmvc` | 4.1.1 | API REST (controladores, JSON) |
| `spring-boot-starter-validation` | 4.1.1 | Validación de datos con Jakarta Bean Validation |
| `spring-boot-starter-data-jpa` | 4.1.1 | Persistencia con JPA / Hibernate |
| `spring-boot-starter-security` | 4.1.1 | Permisos por rol |
| `jjwt` | 0.12.6 | Validar el token JWT emitido por `ms-usuarios` |
| MySQL (contenedor `mysql-siniestrofacil`) | 8.4 | Base de datos `siniestrofacil_talleres` |
| Lombok | 1.18.x | Genera getters, setters y constructores |
| springdoc-openapi (`webmvc-ui`) | 3.1.1 | Documentación OpenAPI + Swagger UI |
| Maven Wrapper | 3.9.x | Compilar y ejecutar sin instalar Maven |
| JUnit 5 + H2 | (incluido) | Pruebas sin necesidad de MySQL |
| Postman | Última | Pruebas automatizadas de la API |

---

## 🧩 Rol en SiniestroFácil

```
Asegurado ──► ms-denuncias (folio, estado RECIBIDA)
                    │
                    ▼  (EP3: Amazon SQS → AWS Lambda)
              ms-polizas: ¿póliza vigente?
                 │ no → denuncia RECHAZADA (no se asigna taller)
                 ▼ sí
              ms-talleres: POST /asignaciones  ← este microservicio
                    │
                    ▼
              ms-denuncias: estado TALLER_ASIGNADO
```

- Los microservicios **no se llaman entre sí**: en la EP2 la asignación se ejecuta desde Postman con el token del ADMIN; en la EP3 la hará la **AWS Lambda**.
- Este servicio **no tiene usuarios propios**: valida el token de `ms-usuarios` con la misma clave `jwt.secret`.

| Rol | Qué puede hacer |
|---|---|
| `ADMIN` | Gestionar talleres (crear, editar, activar/desactivar, eliminar), asignar talleres, ver todas las asignaciones |
| `TALLER` | Ver talleres, ver solo **sus** asignaciones (el `tallerId` viene en el token) y finalizarlas |
| `ASEGURADO` | Ver el listado de talleres |

---

## 📂 Estructura del proyecto

```
ms-talleres/
├── pom.xml
├── mvnw.cmd                                  # Maven Wrapper (copiado de ms-denuncias)
├── DockerfileJar
├── docker/
│   └── create.sql                            # Crea la base siniestrofacil_talleres y sus tablas
└── src/
    ├── main/
    │   ├── java/cl/siniestrofacil/talleres/
    │   │   ├── MsTalleresApplication.java
    │   │   ├── controller/
    │   │   │   ├── TallerController.java      # /talleres
    │   │   │   └── AsignacionController.java  # /asignaciones
    │   │   ├── service/
    │   │   │   ├── TallerService.java         # CRUD y cálculo de cupos
    │   │   │   ├── AsignacionService.java     # Regla de selección de taller
    │   │   │   └── CupoTaller.java
    │   │   ├── model/
    │   │   │   ├── Taller.java                # Entidad JPA (tabla talleres)
    │   │   │   ├── Asignacion.java            # Entidad JPA (tabla asignaciones)
    │   │   │   └── EstadoAsignacion.java      # ASIGNADA, FINALIZADA
    │   │   ├── repository/                    # Spring Data JPA
    │   │   ├── dto/                           # Datos de entrada/salida + validaciones
    │   │   ├── security/                      # Validación JWT y configuración de seguridad
    │   │   ├── exception/                     # GlobalExceptionHandler + excepciones
    │   │   └── config/                        # Swagger y talleres de demostración
    │   └── resources/
    │       └── application.properties         # Puerto 8083, Swagger, MySQL y JWT
    └── test/                                  # Pruebas con H2 en memoria
```

### Modelo de datos (JPA)

```
talleres (1) ─────────< (N) asignaciones
  id, nombre (único), rut (único),     id, folio_denuncia (único),
  direccion, comuna, telefono, email,  estado (ASIGNADA / FINALIZADA),
  cupo_maximo, activo, fecha_creacion  fecha_asignacion, fecha_finalizacion,
                                       taller_id (FK → talleres.id)
```

- `Taller` → `@OneToMany(mappedBy = "taller")`
- `Asignacion` → `@ManyToOne` + `@JoinColumn(name = "taller_id")`

### Talleres de demostración (se crean si la tabla está vacía)

| id | Taller | Comuna | Cupo |
|---|---|---|---|
| 1 | Taller Central (usuario `taller@siniestrofacil.cl`) | Providencia | 3 |
| 2 | Automotriz Norte | Independencia | 2 |

---

## 🐬 Base de datos MySQL en Docker

Usa el **mismo contenedor** `mysql-siniestrofacil` (ver README de `ms-denuncias`), con su **propia base de datos**.

| Dato | Valor |
|---|---|
| Contenedor | `mysql-siniestrofacil` |
| Base de datos | `siniestrofacil_talleres` |
| Usuario / contraseña | `sfuser` / `sfpass` |
| Contraseña de root | `root` |
| Puerto | `3306` |

```powershell
docker start mysql-siniestrofacil
cd ms-talleres
Get-Content docker\create.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
```

Verificar:

```powershell
docker exec -it mysql-siniestrofacil mysql -u sfuser -p
```

```sql
USE siniestrofacil_talleres;
SHOW TABLES;
DESC asignaciones;
```

---

## ▶️ Ejecutar el microservicio

> **Antes:** `mysql-siniestrofacil` corriendo. Para obtener tokens también debe estar corriendo **`ms-usuarios`** (puerto 8080).

Copiar el Maven Wrapper (una sola vez, desde la raíz del repo):

```powershell
Copy-Item ms-denuncias\mvnw.cmd ms-talleres\
Copy-Item -Recurse ms-denuncias\.mvn ms-talleres\
```

```powershell
cd ms-talleres
.\mvnw.cmd spring-boot:run
```

O bien abrir `MsTalleresApplication.java` y presionar **Run** ▶️, o ejecutar el JAR:

```powershell
.\mvnw.cmd clean package
java -jar target\ms-talleres-0.0.1-SNAPSHOT.jar
```

En la consola debe aparecer `Tomcat started on port 8083` y los talleres de demostración creados. Swagger: **http://localhost:8083/**

### Configuración (`application.properties`)

```properties
spring.application.name=ms-talleres
server.port=8083
springdoc.swagger-ui.use-root-path=true

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/siniestrofacil_talleres}
spring.datasource.username=${DB_USER:sfuser}
spring.datasource.password=${DB_PASSWORD:sfpass}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET:siniestrofacil-clave-secreta-desarrollo-jvy0101-2026}
app.datos-demo.habilitado=${DATOS_DEMO:true}
```

---

## 🐳 Microservicio en Docker

```powershell
.\mvnw.cmd clean package
docker build -t ms-talleres -f DockerfileJar .
docker run -d --name ms-talleres --network siniestrofacil-net -p 8083:8083 -e DB_URL=jdbc:mysql://mysql-siniestrofacil:3306/siniestrofacil_talleres ms-talleres
```

Verificar con `docker ps` y `docker logs ms-talleres`.

---

## 🔌 Endpoints de la API

URL base: `http://localhost:8083` · Todas las rutas requieren `Authorization: Bearer <token>`.

### Talleres

| Método | Ruta | Rol | Descripción | Respuestas |
|---|---|---|---|---|
| `GET` | `/talleres?soloActivos=true` | Autenticado | Listar talleres con cupos ocupados y disponibles | `200`, `401` |
| `GET` | `/talleres/disponibles` | Autenticado | Talleres activos con cupo libre | `200`, `401` |
| `GET` | `/talleres/{id}` | Autenticado | Obtener taller | `200`, `400`, `401`, `404` |
| `POST` | `/talleres` | ADMIN | Crear taller | `201`, `400`, `401`, `403`, `409` |
| `PUT` | `/talleres/{id}` | ADMIN | Actualizar taller | `200`, `400`, `401`, `403`, `404`, `409` |
| `PATCH` | `/talleres/{id}/estado` | ADMIN | Activar o desactivar | `200`, `400`, `401`, `403`, `404` |
| `DELETE` | `/talleres/{id}` | ADMIN | Eliminar (solo sin historial) | `204`, `401`, `403`, `404`, `409` |

### Asignaciones

| Método | Ruta | Rol | Descripción | Respuestas |
|---|---|---|---|---|
| `POST` | `/asignaciones` | ADMIN (Lambda en EP3) | Asignar taller a una denuncia | `201`, `400`, `401`, `403`, `409` |
| `GET` | `/asignaciones` | ADMIN | Listar todas | `200`, `401`, `403` |
| `GET` | `/asignaciones/mi-taller` | TALLER | Asignaciones de mi taller | `200`, `401`, `403` |
| `GET` | `/asignaciones/denuncia/{folio}` | ADMIN, TALLER | Asignación de una denuncia | `200`, `401`, `403`, `404` |
| `PATCH` | `/asignaciones/{id}/finalizar` | ADMIN, TALLER | Terminar el caso y liberar el cupo | `200`, `401`, `403`, `404`, `409` |

### 1. Asignar taller — `POST /asignaciones`

**Request:**
```json
{
  "folioDenuncia": "SF-2026-000001"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "folioDenuncia": "SF-2026-000001",
  "tallerId": 1,
  "nombreTaller": "Taller Central",
  "direccionTaller": "Av. Providencia 1234",
  "comunaTaller": "Providencia",
  "estado": "ASIGNADA",
  "fechaAsignacion": "2026-10-05T11:30:00.123456",
  "fechaFinalizacion": null
}
```

### 2. Crear taller — `POST /talleres`

```json
{
  "nombre": "Taller Los Andes",
  "rut": "76444444-4",
  "direccion": "Av. Vicuna Mackenna 4500",
  "comuna": "San Joaquin",
  "telefono": "+56225554444",
  "email": "contacto@tallerlosandes.cl",
  "cupoMaximo": 4
}
```

---

## 🛡 Validaciones y reglas de negocio

| Campo | Reglas | Mensaje de error |
|---|---|---|
| `nombre` | Obligatorio, máx. 100, único | `el nombre es obligatorio` |
| `rut` | Obligatorio, formato `76123456-7`, único | `el rut debe tener formato 76123456-7 (sin puntos y con guion)` |
| `direccion` / `comuna` | Obligatorios | `la direccion es obligatoria` |
| `telefono` | Opcional, 8 a 12 dígitos | `el telefono debe tener entre 8 y 12 digitos (ej: +56223456789)` |
| `email` | Opcional, formato email | `el email no tiene un formato valido` |
| `cupoMaximo` | Obligatorio, entre 1 y 50 | `el cupo maximo debe ser al menos 1` |
| `folioDenuncia` | Obligatorio, formato `SF-AAAA-NNNNNN` | `el folio debe tener formato SF-AAAA-NNNNNN` |

**Reglas de negocio**

| Regla | Respuesta |
|---|---|
| Se elige el taller **activo** con **más cupos libres**; en empate, el de menor id | `201` |
| Ningún taller tiene cupo | `409` — la denuncia se procesa más tarde |
| Una denuncia ya tiene taller asignado | `409` |
| Un TALLER consulta o finaliza una asignación de otro taller | `403` |
| Finalizar una asignación ya finalizada | `409` |
| Bajar el cupo máximo por debajo de los vehículos asignados | `400` |
| Eliminar un taller que ya tuvo asignaciones (se debe desactivar) | `409` |

---

## ⚠️ Manejo de errores

Mismo formato que `ms-denuncias` (`ErrorResponse`):

```json
{
  "timestamp": "2026-10-05T11:31:00.000",
  "status": 409,
  "error": "Conflict",
  "mensaje": "no hay talleres con cupo disponible; la denuncia debe procesarse mas tarde",
  "path": "/asignaciones"
}
```

| Situación | Código |
|---|---|
| Datos inválidos, JSON mal formado, id no numérico, regla de negocio | `400` |
| Sin token, token inválido o vencido | `401` |
| El rol no tiene permiso, o asignación de otro taller | `403` |
| Taller, asignación o ruta inexistente | `404` |
| Nombre/RUT duplicado, sin cupo, ya asignada, ya finalizada, taller con historial | `409` |

---

## 📘 Documentación con Swagger (OpenAPI)

| Recurso | URL |
|---|---|
| Swagger UI (raíz) | http://localhost:8083/ |
| Especificación OpenAPI (JSON) | http://localhost:8083/v3/api-docs |

1. Obtener un token en **ms-usuarios**: http://localhost:8080/ → `POST /auth/login` (admin: `admin@siniestrofacil.cl` / `Admin123!`).
2. En http://localhost:8083/ presionar **Authorize** 🔒 y pegar el token.
3. Probar `GET /talleres` y `POST /asignaciones`.

---

## 🧪 Pruebas con Postman

La colección `postman/ms-talleres.postman_collection.json` contiene **31 peticiones con pruebas automatizadas**. Requiere **ms-usuarios (8080)** y **ms-talleres (8083)** corriendo; la primera carpeta hace login y guarda los tokens.

| Carpeta | Qué prueba |
|---|---|
| 0. Login | Tokens de ADMIN, ASEGURADO y TALLER desde ms-usuarios |
| 1. Talleres | Sin token (401), listar y disponibles (200), obtener (200), inexistente (404), id no numérico (400), crear como asegurado (403), crear (201), nombre duplicado (409), datos inválidos (400), JSON mal formado (400), actualizar (200), desactivar (200) |
| 2. Asignaciones | Asignar como taller (403), folio inválido (400), asignar (201, nunca a un taller inactivo), repetida (409), consultar por folio (200), sin asignación (404), listar (200), mi taller (200, solo taller 1), mi taller como admin (403), listar como asegurado (403), eliminar taller con historial (409), finalizar (200), finalizar otra vez (409), eliminar taller sin historial (204), eliminar de nuevo (404) |

---

## 🧰 Pruebas con Maven

Las pruebas usan **H2 en memoria**: no necesitan MySQL ni `-DskipTests`.

```powershell
.\mvnw.cmd test
```

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Prueba | Qué verifica |
|---|---|
| `MsTalleresApplicationTests` | El contexto levanta y crea los 2 talleres de demostración |
| `SeleccionTallerTest` (5 pruebas) | Elige el de más cupos libres; empate → menor id; ignora talleres llenos e inactivos; sin cupo no elige ninguno |

---

## 📋 Checklist de revisión

- [ ] Base `siniestrofacil_talleres` creada con `create.sql`
- [ ] `.\mvnw.cmd clean package` → `BUILD SUCCESS` con 6 pruebas aprobadas
- [ ] El servicio levanta en el puerto `8083` y crea Taller Central y Automotriz Norte
- [ ] Swagger en `http://localhost:8083/` y **Authorize** con token de ms-usuarios
- [ ] `POST /asignaciones` → `201`; repetida → `409`
- [ ] Usuario taller en `/asignaciones/mi-taller` ve solo las del taller 1
- [ ] Colección Postman completa en verde
- [ ] `SELECT a.folio_denuncia, t.nombre, a.estado FROM asignaciones a JOIN talleres t ON t.id = a.taller_id;` muestra las asignaciones

---

## 🩺 Solución de problemas

| Problema | Causa probable | Solución |
|---|---|---|
| `Unknown database 'siniestrofacil_talleres'` | No se ejecutó `create.sql` | Ejecutar el script de la sección de base de datos |
| `401` con un token válido de ms-usuarios | `jwt.secret` distinto entre servicios | Usar la misma clave en ms-usuarios y ms-talleres |
| Postman: la carpeta *0. Login* falla | ms-usuarios no está corriendo | Levantar ms-usuarios en el puerto 8080 |
| `/asignaciones/mi-taller` responde `400` | El usuario TALLER no tiene `tallerId` | Asignarlo desde ms-usuarios (`PUT /usuarios/{id}`) |
| `409` "no hay talleres con cupo" | Todos los cupos ocupados | Finalizar asignaciones o subir el cupo de un taller |
| `Port 8083 was already in use` | Otro proceso usa el puerto | `netstat -ano \| findstr :8083` → `taskkill /PID <PID> /F` |
| Errores rojos de Lombok en el IDE | El IDE no procesa Lombok | `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` |
