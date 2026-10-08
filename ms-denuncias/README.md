# 🚗 SiniestroFácil — Microservicio `ms-denuncias` (v2)

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Contenedores-2496ED?logo=docker&logoColor=white)

Microservicio REST para **registrar y consultar denuncias de siniestros vehiculares**. Al registrar una denuncia, el servicio entrega de inmediato un **folio único** (`SF-AAAA-NNNNNN`) y deja la denuncia en estado `RECIBIDA`, a la espera de su procesamiento posterior. Las denuncias se guardan en **MySQL** y tanto la base de datos como el microservicio se ejecutan en **contenedores Docker** conectados por una red.

**Novedades de la versión 2**

| Cambio | Detalle |
|---|---|
| Seguridad por rol | Valida el token JWT de `ms-usuarios`. El RUT del asegurado se toma del token, ya no del cuerpo |
| Fotografías | La denuncia guarda referencias (URL) a fotografías: relación `Denuncia 1:N Fotografia` |
| Consultas por rol | El asegurado ve solo sus denuncias; el taller ve **únicamente** las asignadas a él |
| Resultado del procesamiento | `RECHAZADA` (póliza no vigente) o `TALLER_ASIGNADO`. En la EP3 lo registrará la AWS Lambda |
| Recepción del vehículo | El taller asignado confirma que recibió el vehículo (`VEHICULO_RECIBIDO`) |
| Folio persistente | El correlativo continúa desde el último folio guardado en MySQL |
| Pruebas sin MySQL | Los tests usan H2: `mvnw.cmd test` ya no necesita `-DskipTests` |

> Proyecto académico de la asignatura **JVY0101 – Java: Diseño y Construcción de Soluciones Nativas en Nube**, DUOC UC.

---

## 📑 Tabla de contenidos

1. [Tecnologías](#-tecnologías)
2. [Rol en SiniestroFácil](#-rol-en-siniestrofácil)
3. [Estructura del proyecto](#-estructura-del-proyecto)
4. [Requisitos previos](#-requisitos-previos)
5. [Instalación paso a paso](#-instalación-paso-a-paso)
6. [Base de datos MySQL en Docker](#-base-de-datos-mysql-en-docker)
7. [Ejecutar el microservicio](#-ejecutar-el-microservicio)
8. [Microservicio en Docker](#-microservicio-en-docker)
9. [Endpoints de la API](#-endpoints-de-la-api)
10. [Validaciones](#-validaciones)
11. [Manejo de errores](#-manejo-de-errores)
12. [Documentación con Swagger](#-documentación-con-swagger-openapi)
13. [Pruebas con Postman](#-pruebas-con-postman)
14. [Pruebas con Maven](#-pruebas-con-maven)
15. [Checklist de revisión](#-checklist-de-revisión)
16. [Solución de problemas](#-solución-de-problemas)
17. [Equipo](#-equipo)

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
| MySQL (imagen Docker) | 8.4 | Base de datos de las denuncias |
| `mysql-connector-j` | (gestionado por Spring Boot) | Driver JDBC de MySQL |
| Docker Desktop | Última | Contenedores de MySQL y del microservicio |
| Lombok | 1.18.x | Genera getters, setters y constructores |
| springdoc-openapi (`webmvc-ui`) | 3.1.1 | Documentación OpenAPI + Swagger UI |
| Maven Wrapper | 3.9.x | Compilar y ejecutar sin instalar Maven |
| JUnit 5 + H2 | (incluido) | Pruebas sin necesidad de MySQL |
| Postman | Última | Pruebas automatizadas de la API |

---

## 🧩 Rol en SiniestroFácil

El caso exige que **el ingreso de la denuncia y la asignación del taller sean procesos independientes**. Por eso este servicio **registra la denuncia y entrega el folio de inmediato, sin llamar a ningún otro microservicio**.

```
1. Asegurado ──► POST /denuncias ──► folio SF-AAAA-NNNNNN, estado RECIBIDA   (este servicio)
                                         │
              (EP3: Amazon SQS → AWS Lambda; en la EP2 se simula desde Postman)
                                         ▼
2. ms-polizas:  GET /polizas/{numero}/vigencia
       │ vigente = false ──► 3a. PATCH /denuncias/{folio}/resultado  → RECHAZADA (no continúa)
       ▼ vigente = true
3b. ms-talleres: POST /asignaciones ──► PATCH /denuncias/{folio}/resultado → TALLER_ASIGNADO
                                         │
4. Taller ──► GET /denuncias/asignadas ──► PATCH /denuncias/{folio}/recepcion → VEHICULO_RECIBIDO
```

| Rol | Qué puede hacer |
|---|---|
| `ASEGURADO` | Registrar denuncias y consultar **solo las suyas** |
| `TALLER` | Consultar **únicamente** las denuncias asignadas a su taller y confirmar la recepción del vehículo |
| `ADMIN` | Consultar todas y registrar el resultado del procesamiento (en la EP3 lo hará la Lambda) |

### Estados de la denuncia

| Estado | Significado |
|---|---|
| `RECIBIDA` | Registrada con folio, pendiente de procesamiento |
| `RECHAZADA` | La póliza no está vigente: **no continúa** a la asignación de taller |
| `TALLER_ASIGNADO` | Póliza vigente y taller asignado |
| `VEHICULO_RECIBIDO` | El taller confirmó la recepción del vehículo |

---

## 📂 Estructura del proyecto

```
ms-denuncias/
├── pom.xml                               # Dependencias y configuración Maven
├── mvnw.cmd                              # Maven Wrapper para Windows
├── DockerfileJar                         # Imagen Docker del microservicio
├── docker/
│   ├── Dockerfile                        # Imagen Docker de MySQL (sin cambios)
│   ├── create.sql                        # Crea la base y la tabla denuncias (sin cambios)
│   └── migracion-v2.sql                  # Ajuste de la tabla para la v2 (una sola vez)
└── src/
    ├── main/
    │   ├── java/cl/siniestrofacil/denuncias/
    │   │   ├── MsDenunciasApplication.java
    │   │   ├── controller/DenunciaController.java   # Endpoints REST
    │   │   ├── service/DenunciaService.java         # Folio, permisos, resultado y recepción
    │   │   ├── model/
    │   │   │   ├── Denuncia.java                    # Entidad JPA (tabla denuncias)
    │   │   │   ├── Fotografia.java                  # Entidad JPA (tabla fotografias)
    │   │   │   └── EstadoDenuncia.java
    │   │   ├── repository/DenunciaRepository.java
    │   │   ├── dto/                                 # DenunciaRequest, ResultadoRequest, respuestas
    │   │   ├── security/                            # Validación JWT y configuración de seguridad
    │   │   ├── exception/                           # GlobalExceptionHandler + excepciones
    │   │   └── config/                              # Swagger y reloj de Chile
    │   └── resources/application.properties        # Puerto 8081, Swagger, MySQL y JWT
    └── test/                                        # Pruebas con H2 en memoria
```

### Modelo de datos (JPA)

```
denuncias (1) ─────────< (N) fotografias
  folio (PK), patente, rut_asegurado,     id, url, descripcion,
  numero_poliza, fecha_siniestro,         denuncia_folio (FK → denuncias.folio)
  descripcion, estado, fecha_registro,
  taller_id, nombre_taller, motivo_rechazo,
  fecha_resultado, fecha_recepcion
```

- `Denuncia` → `@OneToMany(mappedBy = "denuncia", cascade = ALL)`
- `Fotografia` → `@ManyToOne` + `@JoinColumn(name = "denuncia_folio")`

---

## ✅ Requisitos previos

| Herramienta | Obligatoria | Cómo verificar |
|---|---|---|
| **Windows 10 / 11** con PowerShell | Sí | — |
| **JDK 25** | Sí | `java -version` |
| **Git** | Sí | `git --version` |
| **Docker Desktop** (con WSL 2) | Sí | `docker --version` |
| **Postman** (app de escritorio) | Para pruebas | Abrir la app |
| **ms-usuarios** corriendo (puerto 8080) | Para obtener tokens | http://localhost:8080/ |
| IDE: **VS Code** (Extension Pack for Java) o **IntelliJ IDEA** | Recomendado | — |

> Docker requiere la **virtualización habilitada en la BIOS** (Intel VT-x / AMD-V).

> No es necesario instalar Maven: el proyecto trae **Maven Wrapper** (`mvnw.cmd`).

---

## 📥 Instalación paso a paso

### Paso 1 — Instalar el JDK 25

1. Descarga el instalador `.msi` de un JDK 25 (por ejemplo **Microsoft Build of OpenJDK 25** o **Eclipse Temurin 25**).
2. Ejecuta el instalador y marca **"Set JAVA_HOME variable"** y **"Add to PATH"** si aparecen.
3. Si no aparecen: *Editar las variables de entorno del sistema* → `JAVA_HOME` = carpeta del JDK, y agregar `%JAVA_HOME%\bin` al `Path`.
4. **Cierra y vuelve a abrir** la terminal y verifica:

```powershell
java -version
echo $env:JAVA_HOME
```

### Paso 2 — Clonar el repositorio

```powershell
git clone https://github.com/Almissin/siniestrofacil.git
cd siniestrofacil\ms-denuncias
```

### Paso 3 — Compilar, probar y empaquetar

```powershell
.\mvnw.cmd clean install
```

> En la v2 las pruebas usan H2 en memoria, así que **ya no se necesita `-DskipTests`**.

### Paso 4 — Instalar Docker Desktop

1. Descarga **Docker Desktop para Windows (AMD64)** desde https://www.docker.com/products/docker-desktop/.
2. Instala con la opción **Use WSL 2 instead of Hyper-V (recommended)**.
3. Abre Docker Desktop y espera **Engine running**.

### Paso 5 — Configurar Lombok en el IDE

- **IntelliJ IDEA:** `Settings → Build, Execution, Deployment → Compiler → Annotation Processors` → **Enable annotation processing**.
- **VS Code:** si aparecen errores rojos, `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace`.

---

## 🐬 Base de datos MySQL en Docker

**MySQL debe estar corriendo antes de levantar el microservicio.** Este contenedor también lo usan `ms-usuarios`, `ms-polizas` y `ms-talleres`, cada uno con su propia base de datos.

| Dato | Valor |
|---|---|
| Contenedor | `mysql-siniestrofacil` |
| Base de datos | `siniestrofacil` |
| Usuario / contraseña | `sfuser` / `sfpass` |
| Contraseña de root | `root` |
| Puerto | `3306` |

### Paso 1 — Crear la red Docker (una sola vez)

```powershell
docker network create siniestrofacil-net
```

### Paso 2 — Construir la imagen de MySQL

```powershell
cd ms-denuncias\docker
docker build -t mysql-siniestrofacil .
```

### Paso 3 — Levantar el contenedor de MySQL

```powershell
docker run -d --name mysql-siniestrofacil --network siniestrofacil-net -p 3306:3306 mysql-siniestrofacil
docker logs -f mysql-siniestrofacil
```

Cuando aparezca `ready for connections`, presiona `Ctrl + C`.

### Paso 4 — Migrar la tabla a la versión 2 (una sola vez)

Desde la carpeta `ms-denuncias`:

```powershell
Get-Content docker\migracion-v2.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
```

Deja la columna `estado` como texto para aceptar los nuevos estados. Las columnas nuevas y la tabla `fotografias` las crea Hibernate al iniciar (`ddl-auto=update`).

### Paso 5 — Verificar la base de datos

```powershell
docker exec -it mysql-siniestrofacil mysql -u sfuser -p
```

```sql
USE siniestrofacil;
SHOW TABLES;
DESC denuncias;
DESC fotografias;
```

---

## ▶️ Ejecutar el microservicio

> **Antes:** el contenedor `mysql-siniestrofacil` debe estar corriendo. Para obtener tokens también debe estar corriendo **ms-usuarios** (puerto 8080).

**Opción A — PowerShell**

```powershell
.\mvnw.cmd spring-boot:run
```

**Opción B — Desde el IDE**

Abrir `MsDenunciasApplication.java` y presionar **Run** ▶️.

**Opción C — Ejecutar el JAR generado**

```powershell
.\mvnw.cmd clean package
java -jar target\ms-denuncias-0.0.1-SNAPSHOT.jar
```

En la consola debe aparecer `Tomcat started on port 8081 (http)`. Luego abre **http://localhost:8081/** → Swagger UI.

### Configuración (`application.properties`)

```properties
spring.application.name=ms-denuncias
server.port=8081
springdoc.swagger-ui.use-root-path=true

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/siniestrofacil}
spring.datasource.username=${DB_USER:sfuser}
spring.datasource.password=${DB_PASSWORD:sfpass}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET:siniestrofacil-clave-secreta-desarrollo-jvy0101-2026}
```

> `jwt.secret` debe ser **la misma** que en `ms-usuarios`.

---

## 🐳 Microservicio en Docker

> **Antes:** detén el microservicio si lo tienes corriendo en VS Code, porque ocupa el puerto 8081.

```powershell
.\mvnw.cmd clean install
docker build -t ms-denuncias -f DockerfileJar .
docker run -d --name ms-denuncias --network siniestrofacil-net -p 8081:8081 -e DB_URL=jdbc:mysql://mysql-siniestrofacil:3306/siniestrofacil ms-denuncias
```

| Parte del comando | Qué hace |
|---|---|
| `--name ms-denuncias` | Nombre del contenedor |
| `--network siniestrofacil-net` | Lo conecta a la misma red que MySQL |
| `-p 8081:8081` | Publica el puerto para Swagger y Postman |
| `-e DB_URL=...mysql-siniestrofacil:3306...` | Apunta a MySQL por el nombre del contenedor |

### Comandos útiles

| Acción | Comando |
|---|---|
| Ver contenedores corriendo | `docker ps` |
| Detener | `docker stop ms-denuncias mysql-siniestrofacil` |
| Volver a iniciar (primero MySQL) | `docker start mysql-siniestrofacil` y luego `docker start ms-denuncias` |
| Ver logs | `docker logs -f ms-denuncias` |
| Reconstruir tras cambiar código | `docker rm -f ms-denuncias` y repetir los 3 comandos |

---

## 🔌 Endpoints de la API

URL base: `http://localhost:8081` · Todas las rutas requieren `Authorization: Bearer <token>`.

| Método | Ruta | Rol | Descripción | Respuestas |
|---|---|---|---|---|
| `POST` | `/denuncias` | ASEGURADO | Registra la denuncia y entrega el folio | `201`, `400`, `401`, `403` |
| `GET` | `/denuncias/mis-denuncias` | ASEGURADO | Mis denuncias | `200`, `401`, `403` |
| `GET` | `/denuncias/asignadas` | TALLER | Denuncias asignadas a mi taller | `200`, `400`, `401`, `403` |
| `GET` | `/denuncias?estado=RECIBIDA` | ADMIN | Todas (filtro opcional por estado) | `200`, `400`, `401`, `403` |
| `GET` | `/denuncias/{folio}` | ADMIN, dueño o taller asignado | Consulta por folio | `200`, `401`, `403`, `404` |
| `PATCH` | `/denuncias/{folio}/resultado` | ADMIN (Lambda en EP3) | `RECHAZADA` o `TALLER_ASIGNADO` | `200`, `400`, `401`, `403`, `404`, `409` |
| `PATCH` | `/denuncias/{folio}/recepcion` | TALLER asignado | Confirma la recepción del vehículo | `200`, `401`, `403`, `404`, `409` |

### 1. Registrar denuncia — `POST /denuncias`

**Request** (el RUT del asegurado se toma del token):
```json
{
  "patente": "ABCD12",
  "numeroPoliza": "POL-001",
  "fechaSiniestro": "2026-09-28",
  "descripcion": "choque por alcance en semaforo",
  "fotografias": [
    { "url": "https://fotos.siniestrofacil.cl/ABCD12/parachoque.jpg", "descripcion": "parachoque trasero" }
  ]
}
```

**Response `201 Created`:**
```json
{
  "folio": "SF-2026-000001",
  "patente": "ABCD12",
  "rutAsegurado": "12345678-9",
  "numeroPoliza": "POL-001",
  "fechaSiniestro": "2026-09-28",
  "descripcion": "choque por alcance en semaforo",
  "estado": "RECIBIDA",
  "fechaRegistro": "2026-10-05T10:15:30.123456",
  "fotografias": [
    { "id": 1, "url": "https://fotos.siniestrofacil.cl/ABCD12/parachoque.jpg", "descripcion": "parachoque trasero" }
  ],
  "tallerId": null,
  "nombreTaller": null,
  "motivoRechazo": null,
  "fechaResultado": null,
  "fechaRecepcion": null
}
```

### 2. Registrar resultado — `PATCH /denuncias/{folio}/resultado`

Póliza **no vigente** (la denuncia no continúa a la asignación):
```json
{ "estado": "RECHAZADA", "motivo": "la poliza vencio el 01-06-2025" }
```

Póliza vigente y taller asignado por ms-talleres:
```json
{ "estado": "TALLER_ASIGNADO", "tallerId": 1, "nombreTaller": "Taller Central" }
```

### 3. Confirmar recepción — `PATCH /denuncias/{folio}/recepcion`

Sin cuerpo. Solo el taller asignado (su `tallerId` viene en el token) y solo si la denuncia está en `TALLER_ASIGNADO`.

### Formato del folio

```
SF-2026-000001
│   │    └── correlativo de 6 dígitos (continúa desde el último folio guardado en MySQL)
│   └─────── año actual (hora de Chile)
└─────────── prefijo SiniestroFácil
```

---

## 🛡 Validaciones

| Campo | Reglas | Mensaje de error |
|---|---|---|
| `patente` | Obligatoria | `la patente es obligatoria` |
| | Formato `AA1234` o `ABCD12`, **solo mayúsculas** | `formato de patente invalido` |
| `numeroPoliza` | Obligatorio | `el numero de poliza es obligatorio` |
| `fechaSiniestro` | Obligatoria | `la fecha del siniestro es obligatoria` |
| | No puede ser futura (hoy sí se acepta) | `la fecha del siniestro no puede ser futura` |
| | Formato `AAAA-MM-DD` | `la fecha debe tener formato AAAA-MM-DD` |
| `descripcion` | Obligatoria, máximo 500 caracteres | `la descripcion no puede superar 500 caracteres` |
| `fotografias` | Opcional, máximo 10 | `no se pueden adjuntar mas de 10 fotografias` |
| `fotografias[].url` | Obligatoria, empieza con `http://` o `https://` | `la url debe comenzar con http:// o https://` |

**Reglas del procesamiento**

| Regla | Respuesta |
|---|---|
| Registrar resultado en una denuncia que ya no está `RECIBIDA` | `409` |
| `RECHAZADA` sin `motivo` | `400` |
| `TALLER_ASIGNADO` sin `tallerId` o `nombreTaller` | `400` |
| Resultado distinto de `RECHAZADA` o `TALLER_ASIGNADO` | `400` |
| Confirmar recepción de una denuncia que no está `TALLER_ASIGNADO` | `409` |
| Un taller confirma o consulta una denuncia de otro taller | `403` |
| Un asegurado consulta una denuncia de otro RUT | `403` |

---

## ⚠️ Manejo de errores

Todos los errores pasan por `GlobalExceptionHandler` y responden con el mismo formato (`ErrorResponse`). Se conservan los mensajes de la versión 1.

| Situación | Código |
|---|---|
| Datos que no cumplen las validaciones | `400` |
| JSON mal formado o fecha con formato incorrecto | `400` |
| Sin token, token inválido o vencido | `401` |
| Rol sin permiso, o denuncia de otro usuario/taller | `403` |
| Folio inexistente | `404` |
| Estado que no permite la operación | `409` |

**Ejemplo `404` — folio inexistente:**
```json
{
  "timestamp": "2026-10-05T10:23:00.000",
  "status": 404,
  "error": "Not Found",
  "mensaje": "no existe una denuncia con folio SF-2026-999999",
  "path": "/denuncias/SF-2026-999999"
}
```

**Ejemplo `409` — resultado registrado dos veces:**
```json
{
  "timestamp": "2026-10-05T10:25:00.000",
  "status": 409,
  "error": "Conflict",
  "mensaje": "la denuncia SF-2026-000001 ya fue procesada (estado TALLER_ASIGNADO)",
  "path": "/denuncias/SF-2026-000001/resultado"
}
```

---

## 📘 Documentación con Swagger (OpenAPI)

| Recurso | URL |
|---|---|
| Swagger UI (raíz) | http://localhost:8081/ |
| Swagger UI (ruta estándar) | http://localhost:8081/swagger-ui/index.html |
| Especificación OpenAPI (JSON) | http://localhost:8081/v3/api-docs |

1. Obtener un token en **ms-usuarios** (http://localhost:8080/ → `POST /auth/login`), por ejemplo con `asegurado@siniestrofacil.cl` / `Asegurado123!`.
2. En http://localhost:8081/ presionar **Authorize** 🔒 y pegar el token.
3. `POST /denuncias` → **Try it out** → **Execute** → copiar el folio.
4. `GET /denuncias/{folio}` con el folio.

---

## 🧪 Pruebas con Postman

La colección `postman/ms-denuncias.postman_collection.json` (v2) contiene **41 peticiones con pruebas automatizadas** y reemplaza a la colección de la versión 1.

| Carpeta | Requiere | Qué prueba |
|---|---|---|
| 0. Login | ms-usuarios | Tokens de ADMIN, ASEGURADO y TALLER |
| 1. Registrar denuncia | ms-denuncias | Patente nueva con fotografías (201), patente antigua (201), fecha de hoy (201), folios distintos, sin token (401), como taller (403) |
| 2. Validaciones | ms-denuncias | Las 8 validaciones de la v1 + URL de fotografía inválida (todas 400) |
| 3. Consultas por rol | ms-denuncias | Mi denuncia (200), folio inexistente (404), mis denuncias, listar y filtrar (200), estado inválido (400), asegurado lista todas (403), taller ve denuncia no asignada (403) |
| 4. Procesamiento asíncrono | + ms-polizas y ms-talleres | Simula lo que hará la Lambda en la EP3: vigencia POL-002 → `RECHAZADA`; vigencia POL-001 → asignar taller → `TALLER_ASIGNADO`; errores 409, 400 y 403 |
| 5. Recepción del vehículo | ms-denuncias | Taller ve sus asignadas, confirma recepción (`VEHICULO_RECIBIDO`), recepción de rechazada o pendiente (409), como admin (403) |

Ejecutar con el **Collection Runner** en el orden por defecto. Las variables `baseUrl` (8081), `authUrl` (8080), `polizasUrl` (8082) y `talleresUrl` (8083) ya vienen configuradas.

> ℹ️ La prueba de recepción se adapta sola: si ms-talleres asignó la denuncia a *Taller Central* (id 1, el del usuario taller demo) espera `200`; si la asignó a otro taller espera `403`.

---

## 🧰 Pruebas con Maven

En la v2 las pruebas usan **H2 en memoria**: no necesitan MySQL.

```powershell
.\mvnw.cmd test
```

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Prueba | Qué verifica |
|---|---|
| `MsDenunciasApplicationTests` | El contexto de Spring levanta |
| `FolioTest` (3 pruebas) | Primer folio del año, continuación del correlativo y formato `SF-AAAA-NNNNNN` |
| `EstadoDenunciaTest` (2 pruebas) | Solo `RECIBIDA` admite resultado; solo `TALLER_ASIGNADO` admite recepción |

---

## 📋 Checklist de revisión

- [ ] `java -version` muestra JDK 25
- [ ] `mysql-siniestrofacil` corriendo y `migracion-v2.sql` ejecutado
- [ ] `.\mvnw.cmd clean install` → `BUILD SUCCESS` con 6 pruebas aprobadas
- [ ] El servicio levanta en el puerto `8081`
- [ ] Swagger en `http://localhost:8081/` y **Authorize** con token de ms-usuarios
- [ ] `POST /denuncias` (asegurado) → `201` con folio y estado `RECIBIDA`
- [ ] Taller en `GET /denuncias/asignadas` ve solo sus denuncias
- [ ] Colección Postman v2 completa en verde (carpeta 4 con los 4 servicios arriba)
- [ ] `SELECT folio, estado, nombre_taller FROM denuncias;` y `SELECT * FROM fotografias;` muestran los datos

---

## 🩺 Solución de problemas

| Problema | Causa probable | Solución |
|---|---|---|
| `Data truncated for column 'estado'` | No se ejecutó `migracion-v2.sql` | Ejecutar el Paso 4 de la sección de base de datos |
| `401` en todas las peticiones | Falta el token o venció | Login en ms-usuarios y usar **Authorize** / header `Authorization: Bearer ...` |
| `401` con un token válido de ms-usuarios | `jwt.secret` distinto entre servicios | Usar la misma clave en todos |
| `403` al registrar denuncia | El token no es de un ASEGURADO | Login con `asegurado@siniestrofacil.cl` |
| Postman carpeta 4 falla en 4.1 o 4.4 | ms-polizas o ms-talleres apagados | Levantarlos en 8082 y 8083 |
| `Communications link failure` al iniciar | MySQL no está corriendo | `docker start mysql-siniestrofacil` |
| `Port 8081 was already in use` | Otro proceso usa el puerto | `netstat -ano \| findstr :8081` → `taskkill /PID <PID> /F` |
| Errores rojos de Lombok en el IDE | El IDE no procesa Lombok | `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` |
| `release version 25 not supported` | El JDK activo no es el 25 | Revisar `JAVA_HOME` y reabrir la terminal |

---

## 👥 Equipo

| Integrante |
|---|
| Roberto Bustamante |
| Alex Messin De La Cruz |
| Jahaira Torrijo |

**DUOC UC — Escuela de Informática y Telecomunicaciones**
Asignatura JVY0101 · 2026
