# 🔐 SiniestroFácil — Microservicio `ms-usuarios`

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Contenedores-2496ED?logo=docker&logoColor=white)

Microservicio REST de **autenticación y usuarios**. Permite iniciar sesión y obtener un **token JWT**, registrar asegurados, ver y editar el perfil propio y, para el administrador, gestionar usuarios y roles. Los demás microservicios validan el mismo token para saber **quién** hace la petición y **qué rol** tiene.

> Proyecto académico de la asignatura **JVY0101 – Java: Diseño y Construcción de Soluciones Nativas en Nube**, DUOC UC.

---

## 📑 Tabla de contenidos

1. [Tecnologías](#-tecnologías)
2. [Roles y usuarios de demostración](#-roles-y-usuarios-de-demostración)
3. [Estructura del proyecto](#-estructura-del-proyecto)
4. [Requisitos previos](#-requisitos-previos)
5. [Base de datos MySQL en Docker](#-base-de-datos-mysql-en-docker)
6. [Ejecutar el microservicio](#-ejecutar-el-microservicio)
7. [Microservicio en Docker](#-microservicio-en-docker)
8. [Endpoints de la API](#-endpoints-de-la-api)
9. [Validaciones](#-validaciones)
10. [Manejo de errores](#-manejo-de-errores)
11. [Documentación con Swagger](#-documentación-con-swagger-openapi)
12. [Pruebas con Postman](#-pruebas-con-postman)
13. [Pruebas con Maven](#-pruebas-con-maven)
14. [Checklist de revisión](#-checklist-de-revisión)
15. [Solución de problemas](#-solución-de-problemas)

---

## 🛠 Tecnologías

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java (OpenJDK) | 25 | Lenguaje del microservicio |
| Spring Boot | 4.1.1 | Framework base |
| `spring-boot-starter-webmvc` | 4.1.1 | API REST (controladores, JSON) |
| `spring-boot-starter-validation` | 4.1.1 | Validación de datos con Jakarta Bean Validation |
| `spring-boot-starter-data-jpa` | 4.1.1 | Persistencia con JPA / Hibernate |
| `spring-boot-starter-security` | 4.1.1 | Autenticación y permisos por rol |
| `jjwt` | 0.12.6 | Generar y validar tokens JWT |
| MySQL (contenedor `mysql-siniestrofacil`) | 8.4 | Base de datos `siniestrofacil_usuarios` |
| `mysql-connector-j` | (gestionado por Spring Boot) | Driver JDBC de MySQL |
| Lombok | 1.18.x | Genera getters, setters y constructores |
| springdoc-openapi (`webmvc-ui`) | 3.1.1 | Documentación OpenAPI + Swagger UI |
| Maven Wrapper | 3.9.x | Compilar y ejecutar sin instalar Maven |
| JUnit 5 + H2 | (incluido) | Pruebas sin necesidad de MySQL |
| Postman | Última | Pruebas automatizadas de la API |

---

## 👥 Roles y usuarios de demostración

| Rol | Qué puede hacer |
|---|---|
| `ASEGURADO` | Registrarse, iniciar sesión, ver y editar su perfil, cambiar su contraseña |
| `TALLER` | Iniciar sesión, ver y editar su perfil. Su token incluye el `tallerId` |
| `ADMIN` | Todo lo anterior + crear, listar, editar, activar/desactivar y eliminar usuarios; listar roles |

> El registro público **siempre** crea usuarios `ASEGURADO`. Solo un `ADMIN` puede crear usuarios `TALLER` o `ADMIN`.

Al iniciar, el servicio crea estos usuarios si no existen:

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | `admin@siniestrofacil.cl` | `Admin123!` |
| ASEGURADO (RUT `12345678-9`) | `asegurado@siniestrofacil.cl` | `Asegurado123!` |
| TALLER (`tallerId` = 1) | `taller@siniestrofacil.cl` | `Taller123!` |

---

## 📂 Estructura del proyecto

```
ms-usuarios/
├── pom.xml                                   # Dependencias y configuración Maven
├── mvnw.cmd                                  # Maven Wrapper para Windows (copiado de ms-denuncias)
├── DockerfileJar                             # Imagen Docker del microservicio
├── docker/
│   └── create.sql                            # Crea la base siniestrofacil_usuarios y sus tablas
└── src/
    ├── main/
    │   ├── java/cl/siniestrofacil/usuarios/
    │   │   ├── MsUsuariosApplication.java    # Clase principal
    │   │   ├── controller/
    │   │   │   ├── AuthController.java       # /auth/login y /auth/registro
    │   │   │   ├── PerfilController.java     # /usuarios/me
    │   │   │   ├── UsuarioAdminController.java # /usuarios (solo ADMIN)
    │   │   │   └── RolController.java        # /roles (solo ADMIN)
    │   │   ├── service/
    │   │   │   ├── AuthService.java          # Login y registro
    │   │   │   └── UsuarioService.java       # Perfil y administración
    │   │   ├── model/
    │   │   │   ├── Usuario.java              # Entidad JPA (tabla usuarios)
    │   │   │   ├── Rol.java                  # Entidad JPA (tabla roles)
    │   │   │   └── NombreRol.java            # ADMIN, ASEGURADO, TALLER
    │   │   ├── repository/                   # Spring Data JPA
    │   │   ├── dto/                          # Datos de entrada/salida + validaciones
    │   │   ├── security/                     # JWT, filtro, configuración de seguridad
    │   │   ├── exception/                    # GlobalExceptionHandler + excepciones
    │   │   └── config/                       # Swagger y datos de demostración
    │   └── resources/
    │       └── application.properties        # Puerto 8080, Swagger, MySQL y JWT
    └── test/                                 # Pruebas con H2 en memoria
```

### Arquitectura en capas

```
Cliente (Postman / Swagger / Front)
          │  HTTP + JSON  (Authorization: Bearer <token>)
          ▼
┌──────────────────────────┐
│ JwtAuthenticationFilter  │  ← valida el token y obtiene el rol
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ Controllers              │  ← reciben la petición y activan @Valid
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ AuthService / UsuarioSvc │  ← reglas de negocio, BCrypt, emisión del JWT
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ Usuario / Rol Repository │  ← Spring Data JPA
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ MySQL 8.4                │  ← siniestrofacil_usuarios (contenedor Docker)
└──────────────────────────┘

Cualquier error → GlobalExceptionHandler / ManejadorErroresSeguridad → ErrorResponse (JSON uniforme)
```

### Modelo de datos (JPA)

```
roles (1) ─────────< (N) usuarios
  id                     id, rut (único), nombre, apellido, email (único),
  nombre                 password (BCrypt), telefono, activo, taller_id,
  descripcion            rol_id (FK → roles.id), fecha_creacion
```

- `Rol` → `@OneToMany(mappedBy = "rol")`
- `Usuario` → `@ManyToOne` + `@JoinColumn(name = "rol_id")`

---

## ✅ Requisitos previos

Los mismos de `ms-denuncias` (ver su README): **JDK 25**, **Docker Desktop**, **Git**, **Postman** y **VS Code** con *Extension Pack for Java*.

Además, el contenedor **`mysql-siniestrofacil`** y la red **`siniestrofacil-net`** deben existir (se crean siguiendo el README de `ms-denuncias`).

---

## 🐬 Base de datos MySQL en Docker

Este microservicio **no crea otro contenedor de MySQL**: usa el mismo `mysql-siniestrofacil`, pero con su **propia base de datos** (`siniestrofacil_usuarios`), así cada microservicio es dueño de sus datos.

| Dato | Valor |
|---|---|
| Contenedor | `mysql-siniestrofacil` |
| Base de datos | `siniestrofacil_usuarios` |
| Usuario / contraseña | `sfuser` / `sfpass` |
| Contraseña de root | `root` |
| Puerto | `3306` |

### Paso 1 — Encender MySQL

```powershell
docker start mysql-siniestrofacil
```

### Paso 2 — Crear la base y las tablas (una sola vez)

Desde la carpeta `ms-usuarios`:

```powershell
Get-Content docker\create.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
```

> En PowerShell no funciona `<` para redirigir archivos; por eso se usa `Get-Content ... |`.

### Paso 3 — Verificar

```powershell
docker exec -it mysql-siniestrofacil mysql -u sfuser -p
```

Escribe la contraseña `sfpass` y luego:

```sql
USE siniestrofacil_usuarios;
SHOW TABLES;
DESC usuarios;
```

Deben aparecer las tablas `roles` y `usuarios`. Para salir: `exit`.

---

## ▶️ Ejecutar el microservicio

> **Antes:** el contenedor `mysql-siniestrofacil` debe estar corriendo.

Copiar el Maven Wrapper desde `ms-denuncias` (una sola vez, desde la raíz del repo):

```powershell
Copy-Item ms-denuncias\mvnw.cmd ms-usuarios\
Copy-Item -Recurse ms-denuncias\.mvn ms-usuarios\
```

**Opción A — PowerShell**

```powershell
cd ms-usuarios
.\mvnw.cmd spring-boot:run
```

**Opción B — Desde el IDE**

Abrir `MsUsuariosApplication.java` y presionar **Run** ▶️.

**Opción C — Ejecutar el JAR generado**

```powershell
.\mvnw.cmd clean package
java -jar target\ms-usuarios-0.0.1-SNAPSHOT.jar
```

### Verificar que está arriba

```
Tomcat started on port 8080 (http)
Usuario de demostracion creado: admin@siniestrofacil.cl (ADMIN)
Started MsUsuariosApplication in X.XXX seconds
```

Luego abre **http://localhost:8080/** → Swagger UI.

### Configuración (`application.properties`)

```properties
spring.application.name=ms-usuarios
server.port=8080
springdoc.swagger-ui.use-root-path=true

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/siniestrofacil_usuarios}
spring.datasource.username=${DB_USER:sfuser}
spring.datasource.password=${DB_PASSWORD:sfpass}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET:siniestrofacil-clave-secreta-desarrollo-jvy0101-2026}
jwt.expiracion-ms=${JWT_EXPIRACION_MS:7200000}
app.datos-demo.habilitado=${DATOS_DEMO:true}
```

> `jwt.secret` debe ser **la misma** en todos los microservicios de SiniestroFácil, porque así cada uno puede validar el token que entrega `ms-usuarios`.

---

## 🐳 Microservicio en Docker

> **Antes:** detén el microservicio si lo tienes corriendo en VS Code (`Ctrl + C`), porque ocupa el puerto 8080.

```powershell
.\mvnw.cmd clean package
docker build -t ms-usuarios -f DockerfileJar .
docker run -d --name ms-usuarios --network siniestrofacil-net -p 8080:8080 -e DB_URL=jdbc:mysql://mysql-siniestrofacil:3306/siniestrofacil_usuarios ms-usuarios
```

| Parte del comando | Qué hace |
|---|---|
| `--name ms-usuarios` | Nombre del contenedor |
| `--network siniestrofacil-net` | Lo conecta a la misma red que MySQL |
| `-p 8080:8080` | Publica el puerto para Swagger y Postman |
| `-e DB_URL=...mysql-siniestrofacil:3306...` | Apunta a MySQL por el nombre del contenedor |

Verificar con `docker ps` (deben aparecer `mysql-siniestrofacil` y `ms-usuarios`) y `docker logs ms-usuarios`.

---

## 🔌 Endpoints de la API

URL base: `http://localhost:8080`

| Método | Ruta | Acceso | Descripción | Respuestas |
|---|---|---|---|---|
| `POST` | `/auth/registro` | Público | Registra un asegurado | `201`, `400`, `409` |
| `POST` | `/auth/login` | Público | Entrega el token JWT | `200`, `400`, `401`, `403` |
| `GET` | `/usuarios/me` | Autenticado | Ver mi perfil | `200`, `401` |
| `PUT` | `/usuarios/me` | Autenticado | Editar nombre, apellido y teléfono | `200`, `400`, `401` |
| `PUT` | `/usuarios/me/password` | Autenticado | Cambiar mi contraseña | `204`, `400`, `401` |
| `GET` | `/usuarios?rol=TALLER` | ADMIN | Listar usuarios (filtro opcional) | `200`, `400`, `401`, `403` |
| `GET` | `/usuarios/{id}` | ADMIN | Obtener usuario | `200`, `401`, `403`, `404` |
| `POST` | `/usuarios` | ADMIN | Crear usuario con cualquier rol | `201`, `400`, `401`, `403`, `409` |
| `PUT` | `/usuarios/{id}` | ADMIN | Editar datos, rol y taller | `200`, `400`, `401`, `403`, `404` |
| `PATCH` | `/usuarios/{id}/estado` | ADMIN | Activar o desactivar | `200`, `400`, `401`, `403`, `404` |
| `DELETE` | `/usuarios/{id}` | ADMIN | Eliminar usuario | `204`, `400`, `401`, `403`, `404` |
| `GET` | `/roles` | ADMIN | Listar roles | `200`, `401`, `403` |

### 1. Iniciar sesión — `POST /auth/login`

**Request:**
```json
{
  "email": "admin@siniestrofacil.cl",
  "password": "Admin123!"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 7200,
  "usuario": {
    "id": 1,
    "rut": "11111111-1",
    "nombre": "Administrador",
    "apellido": "SiniestroFacil",
    "email": "admin@siniestrofacil.cl",
    "telefono": null,
    "rol": "ADMIN",
    "tallerId": null,
    "activo": true,
    "fechaCreacion": "2026-10-05T10:15:30.123456"
  }
}
```

El token se envía en las demás peticiones con el header:

```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

### 2. Registrar asegurado — `POST /auth/registro`

```json
{
  "rut": "15678234-K",
  "nombre": "Camila",
  "apellido": "Rojas",
  "email": "camila.rojas@correo.cl",
  "password": "Camila2026!",
  "telefono": "+56912345678"
}
```

Responde `201 Created` con el usuario creado (rol `ASEGURADO`, sin la contraseña).

### Contenido del token

| Dato | Ejemplo | Para qué lo usan los otros microservicios |
|---|---|---|
| `sub` | `asegurado@siniestrofacil.cl` | Identificar al usuario |
| `uid` | `2` | Id del usuario |
| `rut` | `12345678-9` | ms-denuncias asocia la denuncia al asegurado |
| `rol` | `ASEGURADO` | Permisos |
| `tallerId` | `1` (solo TALLER) | El taller ve solo sus denuncias asignadas |

---

## 🛡 Validaciones

| Campo | Reglas | Mensaje de error |
|---|---|---|
| `rut` | Obligatorio, formato `12345678-9` sin puntos y con guion | `el rut debe tener formato 12345678-9 (sin puntos y con guion)` |
| `nombre` / `apellido` | Obligatorios, máximo 80 caracteres | `el nombre es obligatorio` |
| `email` | Obligatorio y con formato de email | `el email no tiene un formato valido` |
| `password` | Entre 8 y 60 caracteres | `la contrasena debe tener entre 8 y 60 caracteres` |
| `telefono` | Opcional, 8 a 12 dígitos, `+` inicial opcional | `el telefono debe tener entre 8 y 12 digitos (ej: +56912345678)` |
| `rol` (admin) | Obligatorio: `ADMIN`, `ASEGURADO` o `TALLER` | `el rol es obligatorio (ADMIN, ASEGURADO o TALLER)` |
| `tallerId` (admin) | Obligatorio solo si el rol es `TALLER` | `un usuario con rol TALLER debe indicar el tallerId` |

**Reglas de negocio:** el email y el RUT no se repiten (`409`); un ADMIN no puede desactivarse, eliminarse ni quitarse el rol a sí mismo (`400`); las contraseñas se guardan cifradas con **BCrypt**.

---

## ⚠️ Manejo de errores

Mismo formato que `ms-denuncias` (`ErrorResponse`). El campo `errores` solo aparece cuando hay detalle por campo.

| Situación | Código |
|---|---|
| Datos inválidos, JSON mal formado, regla de negocio | `400` |
| Sin token, token inválido o vencido, credenciales incorrectas | `401` |
| El rol no tiene permiso, o la cuenta está desactivada | `403` |
| Usuario o ruta inexistente | `404` |
| Email o RUT ya registrado | `409` |

**Ejemplo `403` — un asegurado intenta listar usuarios:**
```json
{
  "timestamp": "2026-10-05T10:20:00.000",
  "status": 403,
  "error": "Forbidden",
  "mensaje": "no tiene permisos para realizar esta operacion",
  "path": "/usuarios"
}
```

**Ejemplo `400` — validación fallida:**
```json
{
  "timestamp": "2026-10-05T10:21:00.000",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "los datos enviados no son validos",
  "path": "/auth/registro",
  "errores": {
    "email": "el email no tiene un formato valido",
    "rut": "el rut debe tener formato 12345678-9 (sin puntos y con guion)"
  }
}
```

---

## 📘 Documentación con Swagger (OpenAPI)

| Recurso | URL |
|---|---|
| Swagger UI (raíz) | http://localhost:8080/ |
| Swagger UI (ruta estándar) | http://localhost:8080/swagger-ui/index.html |
| Especificación OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

### Cómo probar desde Swagger

1. Abre **http://localhost:8080/**.
2. Despliega **`POST /auth/login`** → **Try it out** → **Execute** (el ejemplo ya trae el admin).
3. Copia el valor de `token` de la respuesta.
4. Presiona el botón **Authorize** 🔒 (arriba a la derecha), pega el token y presiona **Authorize**.
5. Ahora puedes probar `GET /usuarios/me`, `GET /usuarios`, etc.
6. Para ver un `403`, repite con el usuario asegurado y prueba `GET /usuarios`.

---

## 🧪 Pruebas con Postman

La colección `postman/ms-usuarios.postman_collection.json` contiene **31 peticiones con pruebas automatizadas**. Los tokens se guardan solos en variables de la colección.

1. Postman → **Import** → `postman/ms-usuarios.postman_collection.json`.
2. Clic derecho en **"ms-usuarios - pruebas"** → **Run collection**.
3. Ejecutar **en el orden por defecto** y revisar que todo quede en verde ✅.

| Carpeta | Qué prueba |
|---|---|
| 1. Autenticación | Login de los 3 roles (200), contraseña incorrecta y email inexistente (401), registro (201), email duplicado (409), datos inválidos (400), JSON mal formado (400) |
| 2. Mi perfil | Ver perfil (200), sin token (401), token inválido (401), editar perfil (200), contraseña actual incorrecta (400) |
| 3. Administración | Asegurado intenta listar (403), listar y filtrar (200), rol inexistente (400), crear TALLER (201), TALLER sin `tallerId` (400), obtener (200), inexistente (404), id no numérico (400), editar (200), desactivar (200), login desactivado (403), admin se desactiva a sí mismo (400), eliminar (204), eliminar de nuevo (404) |
| 4. Roles | Listar roles (200), como taller (403) |

---

## 🧰 Pruebas con Maven

Las pruebas usan **H2 en memoria**, por lo que **no necesitan MySQL** ni `-DskipTests`.

```powershell
.\mvnw.cmd test
```

Resultado esperado:

```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Prueba | Qué verifica |
|---|---|
| `MsUsuariosApplicationTests` | El contexto levanta y se crean los 3 roles y el admin |
| `JwtServiceTest` (4 pruebas) | Token válido con sus datos; rechaza token alterado, firmado con otra clave y vencido |

---

## 📋 Checklist de revisión

- [ ] `java -version` muestra JDK 25
- [ ] `mysql-siniestrofacil` corriendo y base `siniestrofacil_usuarios` creada
- [ ] `.\mvnw.cmd clean package` termina en `BUILD SUCCESS` con 5 pruebas aprobadas
- [ ] El servicio levanta en el puerto `8080` y crea los usuarios demo
- [ ] Swagger carga en `http://localhost:8080/` y el botón **Authorize** funciona
- [ ] Login admin → `200` con token; contraseña incorrecta → `401`
- [ ] Asegurado en `GET /usuarios` → `403`
- [ ] Colección Postman completa en verde
- [ ] `SELECT u.email, r.nombre FROM usuarios u JOIN roles r ON r.id = u.rol_id;` muestra los usuarios con su rol

---

## 🩺 Solución de problemas

| Problema | Causa probable | Solución |
|---|---|---|
| `Unknown database 'siniestrofacil_usuarios'` | No se ejecutó `create.sql` | Paso 2 de la sección de base de datos |
| `Access denied for user 'sfuser'` | Faltan permisos sobre la nueva base | Volver a ejecutar `create.sql` (incluye el `GRANT`) |
| `Communications link failure` | MySQL apagado o iniciando | `docker start mysql-siniestrofacil` y esperar `ready for connections` |
| `Port 8080 was already in use` | Otro proceso usa el puerto | `netstat -ano \| findstr :8080` → `taskkill /PID <PID> /F` |
| `401` con token recién obtenido | Se pegó con la palabra `Bearer` en Swagger o el token venció | En Swagger pegar solo el token; repetir el login |
| Token válido en ms-usuarios pero `401` en otro microservicio | `jwt.secret` distinto entre servicios | Usar la misma clave en todos |
| `mvnw.cmd : El término no se reconoce...` | Falta copiar el wrapper o falta `.\` | Copiar `mvnw.cmd` y `.mvn` desde `ms-denuncias` |
| Errores rojos en `getEmail()`, `setRol()`, etc. | El IDE no procesa Lombok | `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` |
| `release version 25 not supported` | El JDK activo no es el 25 | Revisar `JAVA_HOME` y reabrir la terminal |
