# 📄 SiniestroFácil — Microservicio `ms-polizas`

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Contenedores-2496ED?logo=docker&logoColor=white)

Microservicio REST para **gestionar el listado de pólizas** y **verificar su vigencia**. Una póliza está vigente si está activa y la fecha de hoy está entre su inicio y su término. Antes de asignar un taller a una denuncia se consulta este servicio: si la póliza no está vigente, la denuncia **no continúa** con la asignación.

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
| MySQL (contenedor `mysql-siniestrofacil`) | 8.4 | Base de datos `siniestrofacil_polizas` |
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
              ms-polizas: GET /polizas/{numero}/vigencia   ← este microservicio
                 │ vigente = false → denuncia RECHAZADA (no se asigna taller)
                 ▼ vigente = true
              ms-talleres: asigna taller → denuncia TALLER_ASIGNADO
```

- Los microservicios **no se llaman entre sí**: en la EP2 la consulta de vigencia se hace desde Postman; en la EP3 la hará la **AWS Lambda**.
- Este servicio **no tiene usuarios propios**: valida el token de `ms-usuarios` con la misma clave `jwt.secret`.

| Rol | Qué puede hacer |
|---|---|
| `ADMIN` | Gestionar pólizas (crear, editar, anular, eliminar), consultar todas y su vigencia |
| `ASEGURADO` | Ver **solo sus** pólizas (el RUT viene en el token) y consultar su vigencia |
| `TALLER` | No consulta pólizas (`403`) |

---

## 📂 Estructura del proyecto

```
ms-polizas/
├── pom.xml
├── mvnw.cmd                                  # Maven Wrapper (copiado de ms-denuncias)
├── DockerfileJar
├── docker/
│   └── create.sql                            # Crea la base siniestrofacil_polizas y sus tablas
└── src/
    ├── main/
    │   ├── java/cl/siniestrofacil/polizas/
    │   │   ├── MsPolizasApplication.java
    │   │   ├── controller/PolizaController.java   # /polizas
    │   │   ├── service/
    │   │   │   ├── PolizaService.java              # CRUD y regla de vigencia
    │   │   │   └── ResultadoVigencia.java
    │   │   ├── model/
    │   │   │   ├── Poliza.java                     # Entidad JPA (tabla polizas)
    │   │   │   └── Cobertura.java                  # Entidad JPA (tabla coberturas)
    │   │   ├── repository/PolizaRepository.java
    │   │   ├── dto/                                # Datos de entrada/salida + validaciones
    │   │   ├── security/                           # Validación JWT y configuración de seguridad
    │   │   ├── exception/                          # GlobalExceptionHandler + excepciones
    │   │   └── config/                             # Swagger, reloj de Chile y pólizas demo
    │   └── resources/application.properties       # Puerto 8082, Swagger, MySQL y JWT
    └── test/                                       # Pruebas con H2 en memoria
```

### Modelo de datos (JPA)

```
polizas (1) ─────────< (N) coberturas
  id, numero (único),              id, nombre, monto_maximo,
  rut_asegurado, patente,          poliza_id (FK → polizas.id)
  fecha_inicio, fecha_termino,
  activa, fecha_creacion
```

- `Poliza` → `@OneToMany(mappedBy = "poliza", cascade = ALL, orphanRemoval = true)`: las coberturas se guardan y eliminan junto con la póliza.
- `Cobertura` → `@ManyToOne` + `@JoinColumn(name = "poliza_id")`

### Pólizas de demostración (se crean si la tabla está vacía)

Todas son del asegurado **`12345678-9`** (usuario `asegurado@siniestrofacil.cl` de ms-usuarios):

| Número | Patente | Vigencia | Activa | ¿Vigente hoy? |
|---|---|---|---|---|
| `POL-001` | `ABCD12` | 01-01-2026 al 01-01-2027 | Sí | ✅ Sí |
| `POL-002` | `AB1234` | 01-06-2024 al 01-06-2025 | Sí | ❌ Vencida |
| `POL-003` | `BCDF34` | 01-01-2026 al 01-01-2027 | No | ❌ Anulada |

---

## 🐬 Base de datos MySQL en Docker

Usa el **mismo contenedor** `mysql-siniestrofacil` (ver README de `ms-denuncias`), con su **propia base de datos**.

| Dato | Valor |
|---|---|
| Contenedor | `mysql-siniestrofacil` |
| Base de datos | `siniestrofacil_polizas` |
| Usuario / contraseña | `sfuser` / `sfpass` |
| Contraseña de root | `root` |
| Puerto | `3306` |

```powershell
docker start mysql-siniestrofacil
cd ms-polizas
Get-Content docker\create.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
```

> En PowerShell no funciona `<` para redirigir archivos; por eso se usa `Get-Content ... |`.

Verificar:

```powershell
docker exec -it mysql-siniestrofacil mysql -u sfuser -p
```

```sql
USE siniestrofacil_polizas;
SHOW TABLES;
DESC coberturas;
```

---

## ▶️ Ejecutar el microservicio

> **Antes:** `mysql-siniestrofacil` corriendo. Para obtener tokens también debe estar corriendo **`ms-usuarios`** (puerto 8080).

Copiar el Maven Wrapper (una sola vez, desde la raíz del repo):

```powershell
Copy-Item ms-denuncias\mvnw.cmd ms-polizas\
Copy-Item -Recurse ms-denuncias\.mvn ms-polizas\
```

```powershell
cd ms-polizas
.\mvnw.cmd spring-boot:run
```

O bien abrir `MsPolizasApplication.java` y presionar **Run** ▶️, o ejecutar el JAR:

```powershell
.\mvnw.cmd clean package
java -jar target\ms-polizas-0.0.1-SNAPSHOT.jar
```

En la consola debe aparecer `Tomcat started on port 8082` y las pólizas de demostración creadas. Swagger: **http://localhost:8082/**

### Configuración (`application.properties`)

```properties
spring.application.name=ms-polizas
server.port=8082
springdoc.swagger-ui.use-root-path=true

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/siniestrofacil_polizas}
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
docker build -t ms-polizas -f DockerfileJar .
docker run -d --name ms-polizas --network siniestrofacil-net -p 8082:8082 -e DB_URL=jdbc:mysql://mysql-siniestrofacil:3306/siniestrofacil_polizas ms-polizas
```

Verificar con `docker ps` y `docker logs ms-polizas`.

---

## 🔌 Endpoints de la API

URL base: `http://localhost:8082` · Todas las rutas requieren `Authorization: Bearer <token>`.

| Método | Ruta | Rol | Descripción | Respuestas |
|---|---|---|---|---|
| `GET` | `/polizas?rut=12345678-9` | ADMIN | Listar pólizas (filtro opcional por RUT) | `200`, `401`, `403` |
| `GET` | `/polizas/mis-polizas` | ASEGURADO | Mis pólizas (RUT del token) | `200`, `401`, `403` |
| `GET` | `/polizas/{numero}` | ADMIN, ASEGURADO dueño | Obtener póliza con coberturas | `200`, `401`, `403`, `404` |
| `GET` | `/polizas/{numero}/vigencia` | ADMIN, ASEGURADO dueño | ¿Está vigente hoy? | `200`, `401`, `403`, `404` |
| `POST` | `/polizas` | ADMIN | Crear póliza con coberturas | `201`, `400`, `401`, `403`, `409` |
| `PUT` | `/polizas/{numero}` | ADMIN | Actualizar (reemplaza coberturas) | `200`, `400`, `401`, `403`, `404` |
| `PATCH` | `/polizas/{numero}/estado` | ADMIN | Activar o anular | `200`, `400`, `401`, `403`, `404` |
| `DELETE` | `/polizas/{numero}` | ADMIN | Eliminar (con sus coberturas) | `204`, `401`, `403`, `404` |

### 1. Verificar vigencia — `GET /polizas/POL-002/vigencia`

**Response `200 OK`:**
```json
{
  "numeroPoliza": "POL-002",
  "rutAsegurado": "12345678-9",
  "vigente": false,
  "motivo": "la poliza vencio el 01-06-2025",
  "fechaInicio": "2024-06-01",
  "fechaTermino": "2025-06-01",
  "fechaConsulta": "2026-10-05"
}
```

| Caso | `vigente` | `motivo` |
|---|---|---|
| Activa y dentro del período | `true` | `la poliza esta vigente hasta el 01-01-2027` |
| Ya terminó | `false` | `la poliza vencio el 01-06-2025` |
| Aún no comienza | `false` | `la poliza aun no inicia su vigencia (comienza el ...)` |
| Anulada | `false` | `la poliza se encuentra anulada` |

### 2. Crear póliza — `POST /polizas`

```json
{
  "numero": "POL-010",
  "rutAsegurado": "15678234-K",
  "patente": "BCDF34",
  "fechaInicio": "2026-01-01",
  "fechaTermino": "2027-12-31",
  "coberturas": [
    { "nombre": "Danos propios", "montoMaximo": 12000000 },
    { "nombre": "Responsabilidad civil", "montoMaximo": 8000000 }
  ]
}
```

Responde `201 Created` con la póliza, sus coberturas y el campo calculado `vigente`.

---

## 🛡 Validaciones y reglas de negocio

| Campo | Reglas | Mensaje de error |
|---|---|---|
| `numero` | Obligatorio, formato `POL-001` (3 a 6 dígitos), único | `el numero de poliza debe tener formato POL-001` |
| `rutAsegurado` | Obligatorio, formato `12345678-9` | `el rut debe tener formato 12345678-9 (sin puntos y con guion)` |
| `patente` | Obligatoria, `AA1234` o `ABCD12` en mayúsculas (igual que ms-denuncias) | `formato de patente invalido` |
| `fechaInicio` / `fechaTermino` | Obligatorias, formato `AAAA-MM-DD` | `la fecha de inicio es obligatoria` |
| `coberturas` | Entre 1 y 10 | `la poliza debe tener al menos una cobertura` |
| `coberturas[].nombre` | Obligatorio, máx. 80 | `el nombre de la cobertura es obligatorio` |
| `coberturas[].montoMaximo` | Obligatorio, mayor a 0 | `el monto maximo debe ser mayor a 0` |

**Reglas de negocio**

| Regla | Respuesta |
|---|---|
| La fecha de término debe ser posterior a la de inicio | `400` |
| Número de póliza repetido | `409` |
| Un ASEGURADO consulta una póliza de otro RUT | `403` |
| Una póliza anulada no está vigente aunque las fechas calcen | `vigente: false` |
| La vigencia se evalúa con la fecha de Chile (`America/Santiago`) | — |

---

## ⚠️ Manejo de errores

Mismo formato que `ms-denuncias` (`ErrorResponse`):

```json
{
  "timestamp": "2026-10-05T11:31:00.000",
  "status": 403,
  "error": "Forbidden",
  "mensaje": "la poliza POL-010 no pertenece al usuario",
  "path": "/polizas/POL-010/vigencia"
}
```

| Situación | Código |
|---|---|
| Datos inválidos, JSON mal formado, fechas invertidas | `400` |
| Sin token, token inválido o vencido | `401` |
| El rol no tiene permiso, o póliza de otro asegurado | `403` |
| Póliza o ruta inexistente | `404` |
| Número de póliza duplicado | `409` |

---

## 📘 Documentación con Swagger (OpenAPI)

| Recurso | URL |
|---|---|
| Swagger UI (raíz) | http://localhost:8082/ |
| Especificación OpenAPI (JSON) | http://localhost:8082/v3/api-docs |

1. Obtener un token en **ms-usuarios**: http://localhost:8080/ → `POST /auth/login` (admin: `admin@siniestrofacil.cl` / `Admin123!`).
2. En http://localhost:8082/ presionar **Authorize** 🔒 y pegar el token.
3. Probar `GET /polizas/POL-001/vigencia` y `GET /polizas/POL-002/vigencia`.

---

## 🧪 Pruebas con Postman

La colección `postman/ms-polizas.postman_collection.json` contiene **27 peticiones con pruebas automatizadas**. Requiere **ms-usuarios (8080)** y **ms-polizas (8082)** corriendo; la primera carpeta hace login y guarda los tokens.

| Carpeta | Qué prueba |
|---|---|
| 0. Login | Tokens de ADMIN, ASEGURADO y TALLER desde ms-usuarios |
| 1. Consultas | Sin token (401), asegurado lista todas (403), listar y filtrar (200), mis pólizas (200), obtener con coberturas (200), taller (403), inexistente (404) |
| 2. Vigencia | POL-001 vigente, POL-002 vencida, POL-003 anulada, asegurado dueño (200), inexistente (404) |
| 3. Administración | Crear como asegurado (403), crear (201), duplicada (409), datos inválidos (400), fechas invertidas (400), JSON mal formado (400), actualizar coberturas (200), póliza ajena (403), anular (200), eliminar (204), eliminar de nuevo (404) |

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
| `MsPolizasApplicationTests` | El contexto levanta y crea las 3 pólizas demo con sus coberturas |
| `VigenciaPolizaTest` (5 pruebas) | Vigente dentro del período; primer y último día vigentes; vencida; aún no comienza; anulada |

---

## 📋 Checklist de revisión

- [ ] Base `siniestrofacil_polizas` creada con `create.sql`
- [ ] `.\mvnw.cmd clean package` → `BUILD SUCCESS` con 6 pruebas aprobadas
- [ ] El servicio levanta en el puerto `8082` y crea POL-001, POL-002 y POL-003
- [ ] Swagger en `http://localhost:8082/` y **Authorize** con token de ms-usuarios
- [ ] `GET /polizas/POL-001/vigencia` → `vigente: true`; `POL-002` → `vigente: false`
- [ ] Usuario taller en `/polizas/POL-001` → `403`
- [ ] Colección Postman completa en verde
- [ ] `SELECT p.numero, c.nombre, c.monto_maximo FROM polizas p JOIN coberturas c ON c.poliza_id = p.id;` muestra las coberturas

---

## 🩺 Solución de problemas

| Problema | Causa probable | Solución |
|---|---|---|
| `Unknown database 'siniestrofacil_polizas'` | No se ejecutó `create.sql` | Ejecutar el script de la sección de base de datos |
| `401` con un token válido de ms-usuarios | `jwt.secret` distinto entre servicios | Usar la misma clave en ms-usuarios y ms-polizas |
| Postman: la carpeta *0. Login* falla | ms-usuarios no está corriendo | Levantar ms-usuarios en el puerto 8080 |
| `403` del asegurado en su propia póliza | El RUT del token no coincide con `rutAsegurado` | Revisar que la póliza sea del RUT `12345678-9` |
| `Port 8082 was already in use` | Otro proceso usa el puerto | `netstat -ano \| findstr :8082` → `taskkill /PID <PID> /F` |
| Errores rojos de Lombok en el IDE | El IDE no procesa Lombok | `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` |
