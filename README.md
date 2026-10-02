# 🚗 SiniestroFácil — Microservicio `ms-denuncias`

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)

Microservicio REST para **registrar y consultar denuncias de siniestros vehiculares**. Al registrar una denuncia, el servicio entrega de inmediato un **folio único** (`SF-AAAA-NNNNNN`) y deja la denuncia en estado `RECIBIDA`, a la espera de su procesamiento posterior.

> Proyecto académico de la asignatura **JVY0101 – Java: Diseño y Construcción de Soluciones Nativas en Nube**, DUOC UC.

---

## 📑 Tabla de contenidos

1. [Tecnologías](#-tecnologías)
2. [Estructura del proyecto](#-estructura-del-proyecto)
3. [Requisitos previos](#-requisitos-previos)
4. [Instalación paso a paso](#-instalación-paso-a-paso)
5. [Ejecutar el microservicio](#-ejecutar-el-microservicio)
6. [Endpoints de la API](#-endpoints-de-la-api)
7. [Validaciones](#-validaciones)
8. [Manejo de errores](#-manejo-de-errores)
9. [Documentación con Swagger](#-documentación-con-swagger-openapi)
10. [Pruebas con Postman](#-pruebas-con-postman)
11. [Pruebas con Maven](#-pruebas-con-maven)
12. [Checklist de revisión](#-checklist-de-revisión)
13. [Solución de problemas](#-solución-de-problemas)
14. [Limitaciones y próximos pasos](#-limitaciones-y-próximos-pasos)
15. [Equipo](#-equipo)

---

## 🛠 Tecnologías

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java (OpenJDK) | 25 | Lenguaje del microservicio |
| Spring Boot | 4.1.1 | Framework base |
| `spring-boot-starter-webmvc` | 4.1.1 | API REST (controladores, JSON) |
| `spring-boot-starter-validation` | 4.1.1 | Validación de datos con Jakarta Bean Validation |
| Lombok | 1.18.x | Genera getters, setters y constructores |
| springdoc-openapi (`webmvc-ui`) | 3.1.1 | Documentación OpenAPI + Swagger UI |
| Maven Wrapper | 3.9.x | Compilar y ejecutar sin instalar Maven |
| Postman | Última | Pruebas automatizadas de la API |
| JUnit 5 | (incluido) | Prueba de carga del contexto |

---

## 📂 Estructura del proyecto

```
siniestrofacil/
├── ms-denuncias/                         # Microservicio de denuncias
│   ├── pom.xml                           # Dependencias y configuración Maven
│   ├── mvnw / mvnw.cmd                   # Maven Wrapper (Linux-Mac / Windows)
│   └── src/
│       ├── main/
│       │   ├── java/cl/siniestrofacil/denuncias/
│       │   │   ├── MsDenunciasApplication.java      # Clase principal
│       │   │   ├── controller/
│       │   │   │   └── DenunciaController.java      # Endpoints REST
│       │   │   ├── service/
│       │   │   │   └── DenunciaService.java         # Lógica de negocio y folio
│       │   │   ├── model/
│       │   │   │   └── Denuncia.java                # Entidad de la denuncia
│       │   │   ├── dto/
│       │   │   │   ├── DenunciaRequest.java         # Datos de entrada + validaciones
│       │   │   │   └── ErrorResponse.java           # Formato común de errores
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java  # Manejo centralizado de errores
│       │   │       └── DenunciaNoEncontradaException.java
│       │   └── resources/
│       │       └── application.properties           # Puerto 8081 + Swagger en la raíz
│       └── test/java/cl/siniestrofacil/denuncias/
│           └── MsDenunciasApplicationTests.java
└── postman/
    └── ms-denuncias.postman_collection.json         # Colección de pruebas automatizadas
```

### Arquitectura en capas

```
Cliente (Postman / Swagger / Front)
          │  HTTP + JSON
          ▼
┌──────────────────────┐
│  DenunciaController  │  ← recibe la petición y activa @Valid
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│   DenunciaService    │  ← genera folio, fija estado RECIBIDA, guarda
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ ConcurrentHashMap    │  ← almacenamiento en memoria (sin BD por ahora)
└──────────────────────┘

Cualquier error → GlobalExceptionHandler → ErrorResponse (JSON uniforme)
```

---

## ✅ Requisitos previos

| Herramienta | Obligatoria | Cómo verificar |
|---|---|---|
| **JDK 25** | Sí | `java -version` |
| **Git** | Sí | `git --version` |
| **Postman** (app de escritorio) | Para pruebas | Abrir la app |
| **Node.js + Newman** | Opcional (pruebas por terminal) | `newman -v` |
| IDE: **VS Code** (Extension Pack for Java) o **IntelliJ IDEA** | Recomendado | — |

> No es necesario instalar Maven: el proyecto trae **Maven Wrapper** (`mvnw` / `mvnw.cmd`), que descarga la versión correcta automáticamente.

---

## 📥 Instalación paso a paso

### Paso 1 — Instalar el JDK 25

1. Descarga un JDK 25 (por ejemplo **Microsoft Build of OpenJDK 25** o **Eclipse Temurin 25**).
2. Instálalo y configura la variable de entorno `JAVA_HOME` apuntando a la carpeta del JDK.
3. Agrega `%JAVA_HOME%\bin` (Windows) o `$JAVA_HOME/bin` (Linux/Mac) al `PATH`.
4. Verifica en una terminal nueva:

```bash
java -version
# Debe mostrar: openjdk version "25..."
```

### Paso 2 — Clonar el repositorio

```bash
git clone https://github.com/<usuario>/<repositorio>.git
cd <repositorio>/ms-denuncias
```

### Paso 3 — Dar permisos al wrapper (solo Linux / Mac)

```bash
chmod +x mvnw
```

### Paso 4 — Compilar y descargar dependencias

**Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd clean install
```

**Linux / Mac:**
```bash
./mvnw clean install
```

Si todo está bien, verás al final:

```
[INFO] BUILD SUCCESS
```

### Paso 5 — Configurar Lombok en el IDE

Lombok genera código al compilar; el IDE necesita saberlo para no marcar errores falsos.

- **IntelliJ IDEA:** `Settings → Build, Execution, Deployment → Compiler → Annotation Processors` → marcar **Enable annotation processing**. Instalar el plugin *Lombok* si no viene incluido.
- **VS Code:** el *Extension Pack for Java* ya incluye soporte para Lombok. Si aparecen errores rojos, ejecutar `Java: Clean Java Language Server Workspace` desde la paleta de comandos (`Ctrl+Shift+P`).

---

## ▶️ Ejecutar el microservicio

**Opción A — Terminal**

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

**Opción B — Desde el IDE**

Abrir `MsDenunciasApplication.java` y presionar **Run** ▶️.

**Opción C — Ejecutar el JAR generado**

```bash
./mvnw clean package
java -jar target/ms-denuncias-0.0.1-SNAPSHOT.jar
```

### Verificar que está arriba

En la consola debe aparecer algo como:

```
Tomcat started on port 8081 (http)
Started MsDenunciasApplication in X.XXX seconds
```

Luego abre en el navegador: **http://localhost:8081/** → debería cargar Swagger UI.

### Configuración (`application.properties`)

```properties
spring.application.name=ms-denuncias
server.port=8081
# abrir Swagger UI en la raiz (http://localhost:8081/)
springdoc.swagger-ui.use-root-path=true
```

---

## 🔌 Endpoints de la API

URL base: `http://localhost:8081`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/denuncias` | Registra una denuncia y entrega el folio | `201`, `400` |
| `GET` | `/denuncias/{folio}` | Consulta una denuncia por su folio | `200`, `404` |

### 1. Registrar denuncia — `POST /denuncias`

**Request:**
```json
{
  "patente": "ABCD12",
  "rutAsegurado": "12345678-9",
  "numeroPoliza": "POL-001",
  "fechaSiniestro": "2026-09-28",
  "descripcion": "choque por alcance en semaforo"
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
  "fechaRegistro": "2026-10-02T10:15:30.123456"
}
```

**Con cURL:**
```bash
curl -X POST http://localhost:8081/denuncias \
  -H "Content-Type: application/json" \
  -d '{"patente":"ABCD12","rutAsegurado":"12345678-9","numeroPoliza":"POL-001","fechaSiniestro":"2026-09-28","descripcion":"choque por alcance en semaforo"}'
```

### 2. Consultar por folio — `GET /denuncias/{folio}`

```bash
curl http://localhost:8081/denuncias/SF-2026-000001
```

- `200 OK` → devuelve la denuncia completa.
- `404 Not Found` → el folio no existe (ver [Manejo de errores](#-manejo-de-errores)).

### Formato del folio

```
SF-2026-000001
│   │    └── correlativo de 6 dígitos (se reinicia al reiniciar el servicio)
│   └─────── año actual
└─────────── prefijo SiniestroFácil
```

---

## 🛡 Validaciones

Las validaciones están en `DenunciaRequest.java` y se activan con `@Valid` en el controlador.

| Campo | Reglas | Mensaje de error |
|---|---|---|
| `patente` | Obligatoria | `la patente es obligatoria` |
| | Formato chileno: `AA1234` (antiguo) o `ABCD12` (nuevo), **solo mayúsculas** | `formato de patente invalido` |
| `rutAsegurado` | Obligatorio | `el rut del asegurado es obligatorio` |
| `numeroPoliza` | Obligatorio | `el numero de poliza es obligatorio` |
| `fechaSiniestro` | Obligatoria | `la fecha del siniestro es obligatoria` |
| | No puede ser futura (hoy sí se acepta) | `la fecha del siniestro no puede ser futura` |
| | Formato `AAAA-MM-DD` | `la fecha debe tener formato AAAA-MM-DD` |
| `descripcion` | Obligatoria | `la descripcion es obligatoria` |
| | Máximo 500 caracteres | `la descripcion no puede superar 500 caracteres` |

**Expresión regular de la patente:**

```regex
^[A-Z]{2}\d{4}$|^[A-Z]{4}\d{2}$
```

| Ejemplo | ¿Válida? | Motivo |
|---|---|---|
| `ABCD12` | ✅ | Formato nuevo |
| `AB1234` | ✅ | Formato antiguo |
| `ABC123` | ❌ | No calza con ningún formato |
| `abcd12` | ❌ | Minúsculas |

> **Nota:** `@NotBlank` también rechaza textos con solo espacios (ej. `"  "`).

---

## ⚠️ Manejo de errores

Todos los errores pasan por `GlobalExceptionHandler` y responden con el mismo formato (`ErrorResponse`). El campo `errores` solo aparece cuando hay detalle por campo.

| Situación | Código | Excepción capturada |
|---|---|---|
| Datos que no cumplen las validaciones | `400` | `MethodArgumentNotValidException` |
| JSON mal formado o fecha con formato incorrecto | `400` | `HttpMessageNotReadableException` |
| Folio inexistente | `404` | `DenunciaNoEncontradaException` |

**Ejemplo `400` — validación fallida:**
```json
{
  "timestamp": "2026-10-02T10:20:00.000",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "la denuncia tiene datos invalidos",
  "path": "/denuncias",
  "errores": {
    "fechaSiniestro": "la fecha del siniestro no puede ser futura",
    "patente": "formato de patente invalido"
  }
}
```

**Ejemplo `400` — fecha con formato incorrecto (`28-09-2026`):**
```json
{
  "timestamp": "2026-10-02T10:21:00.000",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "el campo fechaSiniestro tiene un formato invalido",
  "path": "/denuncias",
  "errores": {
    "fechaSiniestro": "la fecha debe tener formato AAAA-MM-DD"
  }
}
```

**Ejemplo `400` — JSON mal formado:**
```json
{
  "timestamp": "2026-10-02T10:22:00.000",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "el cuerpo de la peticion no es un json valido",
  "path": "/denuncias"
}
```

**Ejemplo `404` — folio inexistente:**
```json
{
  "timestamp": "2026-10-02T10:23:00.000",
  "status": 404,
  "error": "Not Found",
  "mensaje": "no existe una denuncia con folio SF-2026-999999",
  "path": "/denuncias/SF-2026-999999"
}
```

> Los errores de un mismo campo se unen con `; ` y los campos se ordenan alfabéticamente para que la respuesta sea siempre igual.

---

## 📘 Documentación con Swagger (OpenAPI)

La documentación se genera automáticamente con **springdoc-openapi** a partir de los controladores y DTOs.

| Recurso | URL |
|---|---|
| Swagger UI (raíz) | http://localhost:8081/ |
| Swagger UI (ruta estándar) | http://localhost:8081/swagger-ui/index.html |
| Especificación OpenAPI (JSON) | http://localhost:8081/v3/api-docs |
| Especificación OpenAPI (YAML) | http://localhost:8081/v3/api-docs.yaml |

### Cómo probar desde Swagger

1. Levanta el microservicio y abre **http://localhost:8081/**.
2. Despliega **`POST /denuncias`** → clic en **Try it out**.
3. Pega el JSON de ejemplo en el cuerpo y presiona **Execute**.
4. Revisa el código de respuesta (`201`) y copia el `folio` generado.
5. Despliega **`GET /denuncias/{folio}`** → **Try it out** → pega el folio → **Execute**.
6. Prueba también con datos inválidos para ver las respuestas `400` y con un folio falso para ver el `404`.

### Dependencia en `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.1</version>
</dependency>
```

---

## 🧪 Pruebas con Postman

La colección `postman/ms-denuncias.postman_collection.json` contiene **12 peticiones con pruebas automatizadas** (scripts `pm.test`).

### Paso 1 — Importar la colección

1. Abre Postman → **Import**.
2. Selecciona el archivo `postman/ms-denuncias.postman_collection.json`.
3. Aparecerá la colección **"ms-denuncias - pruebas"**.

### Paso 2 — Revisar las variables

La colección trae sus propias variables (pestaña **Variables** de la colección):

| Variable | Valor | Uso |
|---|---|---|
| `baseUrl` | `http://localhost:8081` | URL del microservicio |
| `folio` | *(se llena sola)* | El primer POST guarda aquí el folio generado |

### Paso 3 — Ejecutar con el Collection Runner

1. Asegúrate de que el microservicio esté corriendo.
2. Clic derecho en la colección → **Run collection**.
3. **Ejecuta en el orden por defecto** (la consulta por folio usa el folio guardado por el primer POST).
4. Presiona **Run** y revisa que todas las pruebas queden en verde ✅.

### Casos cubiertos

**1. Registrar denuncia (casos válidos)** — esperan `201`

| Prueba | Qué verifica |
|---|---|
| Patente nueva (`ABCD12`) | Folio `SF-AAAA-NNNNNN`, año actual, estado `RECIBIDA`, datos devueltos, fecha de registro. Guarda el folio |
| Patente antigua (`AB1234`) | Acepta formato antiguo y genera un folio distinto |
| Fecha de hoy | La fecha actual no se considera futura |

**2. Consultar denuncia por folio**

| Prueba | Esperado |
|---|---|
| Folio existente | `200` con los datos registrados |
| Folio inexistente (`SF-2026-999999`) | `404` con mensaje `no existe una denuncia con folio ...` |

**3. Validaciones** — todas esperan `400` y un `mensaje` no vacío

| Prueba | Error esperado |
|---|---|
| Patente `ABC123` | `formato de patente invalido` |
| Patente en minúsculas `abcd12` | `formato de patente invalido` |
| Fecha futura `2099-01-01` | `la fecha del siniestro no puede ser futura` |
| Fecha `28-09-2026` | `la fecha debe tener formato AAAA-MM-DD` |
| Descripción de 501 caracteres | `la descripcion no puede superar 500 caracteres` |
| Campos obligatorios en blanco | Un error por cada campo vacío |
| Body vacío `{}` | Error en los 5 campos |
| JSON mal formado | `el cuerpo de la peticion no es un json valido` |

### (Opcional) Ejecutar por terminal con Newman

```bash
npm install -g newman
newman run postman/ms-denuncias.postman_collection.json
```

> ⚠️ Las denuncias se guardan **en memoria**: si reinicias el microservicio, se borran y el correlativo del folio vuelve a `000001`.

> ℹ️ En `postman/collections/` hay peticiones sueltas antiguas que apuntan al puerto **8080**. El servicio corre en **8081**; usa la colección `ms-denuncias.postman_collection.json`, que ya tiene el puerto correcto.

---

## 🧰 Pruebas con Maven

El proyecto incluye una prueba que verifica que el contexto de Spring levanta correctamente.

```bash
# Windows
.\mvnw.cmd test

# Linux / Mac
./mvnw test
```

Resultado esperado:

```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Para una verificación completa (compilar + probar + empaquetar):

```bash
./mvnw clean verify
```

---

## 📋 Checklist de revisión

Usa esta lista para revisar el proyecto antes de una entrega o un *pull request*:

**Instalación y ejecución**
- [ ] `java -version` muestra JDK 25
- [ ] `mvnw clean install` termina en `BUILD SUCCESS`
- [ ] El servicio levanta en el puerto `8081` sin errores

**Swagger**
- [ ] `http://localhost:8081/` carga Swagger UI
- [ ] Aparecen los endpoints `POST /denuncias` y `GET /denuncias/{folio}`
- [ ] `http://localhost:8081/v3/api-docs` devuelve el JSON de OpenAPI

**Funcionalidad**
- [ ] `POST` válido responde `201` con folio `SF-AAAA-NNNNNN` y estado `RECIBIDA`
- [ ] `GET` con folio existente responde `200`
- [ ] `GET` con folio inexistente responde `404`

**Validaciones**
- [ ] Patente inválida o en minúsculas → `400`
- [ ] Fecha futura → `400`; fecha de hoy → `201`
- [ ] Fecha con formato `DD-MM-AAAA` → `400`
- [ ] Descripción > 500 caracteres → `400`
- [ ] Campos vacíos / body `{}` / JSON roto → `400`

**Postman**
- [ ] Colección importada y ejecutada con el Runner
- [ ] Todas las pruebas en verde

**Código**
- [ ] Sin errores de Lombok en el IDE
- [ ] `mvnw test` pasa sin fallos

---

## 🩺 Solución de problemas

| Problema | Causa probable | Solución |
|---|---|---|
| `release version 25 not supported` | El JDK activo no es el 25 | Instalar JDK 25 y revisar `JAVA_HOME` |
| `Port 8081 was already in use` | Otro proceso usa el puerto | Cerrar ese proceso o cambiar `server.port` en `application.properties` |
| `./mvnw: Permission denied` | Falta permiso de ejecución | `chmod +x mvnw` |
| Errores rojos en `getFolio()`, `setPatente()`, etc. | El IDE no procesa Lombok | Habilitar *annotation processing* / limpiar workspace de Java |
| Swagger da `404` | El servicio no levantó o la URL está mal | Revisar consola y usar `/swagger-ui/index.html` |
| Postman: `Could not get response` | Servicio apagado o puerto incorrecto | Levantar el servicio y revisar que `baseUrl` sea `http://localhost:8081` |
| Prueba "Consultar folio existente" falla | Se ejecutó sola o fuera de orden | Ejecutar la colección completa en orden con el Runner |
| Advertencia `sun.misc.Unsafe` al compilar | Lombok usa una API antigua del JDK | Es solo una advertencia; no afecta la ejecución |

---

## 🚧 Limitaciones y próximos pasos

**Limitaciones actuales**
- Almacenamiento **en memoria** (`ConcurrentHashMap`): los datos se pierden al reiniciar.
- El RUT solo se valida como obligatorio, no se verifica el dígito verificador.
- El estado queda fijo en `RECIBIDA` (aún no existe el procesamiento asíncrono).

**Próximos pasos sugeridos**
- [ ] Persistencia con base de datos (Spring Data JPA)
- [ ] Validador personalizado de RUT chileno (módulo 11)
- [ ] Procesamiento asíncrono para cambiar el estado de la denuncia
- [ ] Anotaciones `@Operation` / `@Schema` para enriquecer Swagger
- [ ] Pruebas unitarias del servicio y pruebas de controlador con MockMvc
- [ ] Contenerización con Docker y despliegue en la nube (AWS)

---

## 👥 Equipo

| Integrante |
|---|
| Roberto Bustamante |
| Alex Messin De La Cruz |
| Jahaira Torrijo |

**DUOC UC — Escuela de Informática y Telecomunicaciones**
Asignatura JVY0101 · 2026
