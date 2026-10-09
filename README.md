# 🚗 SiniestroFácil — Microservicios (EP2)

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Contenedores-2496ED?logo=docker&logoColor=white)

Microservicio REST para **registrar y consultar denuncias de siniestros vehiculares**. Al registrar una denuncia, el servicio entrega de inmediato un **folio único** (`SF-AAAA-NNNNNN`) y deja la denuncia en estado `RECIBIDA`, a la espera de su procesamiento posterior. Las denuncias se guardan en **MySQL** y tanto la base de datos como el microservicio se ejecutan en **contenedores Docker** conectados por una red.

>

---

## 📑 Tabla de contenidos

1. [Microservicios](#-microservicios)
2. [Arquitectura](#-arquitectura)
3. [Roles y usuarios de demostración](#-roles-y-usuarios-de-demostración)
4. [Tecnologías](#-tecnologías)
5. [Estructura del repositorio](#-estructura-del-repositorio)
6. [Requisitos previos](#-requisitos-previos)
7. [Paso 1 — Clonar el repositorio](#paso-1--clonar-el-repositorio)
8. [Paso 2 — Encender MySQL en Docker](#paso-2--encender-mysql-en-docker)
9. [Paso 3 — Crear las bases de datos](#paso-3--crear-las-bases-de-datos)
10. [Paso 4 — Compilar, probar y empaquetar con Maven](#paso-4--compilar-probar-y-empaquetar-con-maven)
11. [Paso 5 — Levantar los microservicios](#paso-5--levantar-los-microservicios)
12. [Paso 6 — Verificar en Swagger](#paso-6--verificar-en-swagger)
13. [Paso 7 — Verificar los datos en MySQL](#paso-7--verificar-los-datos-en-mysql)
14. [Paso 8 — Pruebas con Postman](#paso-8--pruebas-con-postman)
15. [Apagar todo](#-apagar-todo)
16. [Evidencias](#-evidencias)
17. [Solución de problemas](#-solución-de-problemas)
18. [Próxima etapa: AWS (EP3)](#️-próxima-etapa-aws-ep3)
19. [Equipo](#-equipo)

---

## 🧩 Microservicios

| Microservicio | Puerto | Base de datos | Qué hace | Documentación |
|---|---|---|---|---|
| **ms-usuarios** | `8080` | `siniestrofacil_usuarios` | Inicio de sesión con **JWT**, registro de asegurados, perfil y administración de usuarios y roles | [README](ms-usuarios/README.md) |
| **ms-denuncias** | `8081` | `siniestrofacil` | Registro de denuncias con **folio** `SF-AAAA-NNNNNN`, fotografías, resultado del procesamiento y recepción del vehículo | [README](ms-denuncias/README.md) |
| **ms-polizas** | `8082` | `siniestrofacil_polizas` | Listado de pólizas con coberturas y **verificación de vigencia** | [README](ms-polizas/README.md) |
| **ms-talleres** | `8083` | `siniestrofacil_talleres` | Listado de talleres en convenio y **asignación** del taller con cupo | [README](ms-talleres/README.md) |

Todos comparten la misma configuración: **Java 25**, **Spring Boot 4.1.1**, arquitectura en capas (**Controller → Service → Repository → Model**), validaciones, manejo centralizado de errores con el mismo formato JSON, **Swagger** en la raíz de cada puerto y pruebas con **H2** (no necesitan MySQL).

---

## 🏗 Arquitectura

```
                         Cliente (Postman / Swagger)
                                   │  HTTP + JSON + token JWT
       ┌───────────────┬───────────┴───────┬───────────────────┐
       ▼               ▼                   ▼                   ▼
┌─────────────┐ ┌──────────────┐   ┌──────────────┐   ┌───────────────┐
│ ms-usuarios │ │ ms-denuncias │   │  ms-polizas  │   │  ms-talleres  │
│    8080     │ │     8081     │   │     8082     │   │     8083      │
│ emite el JWT│ │ valida JWT   │   │ valida JWT   │   │  valida JWT   │
└──────┬──────┘ └──────┬───────┘   └──────┬───────┘   └───────┬───────┘
       ▼               ▼                  ▼                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│              Contenedor Docker: mysql-siniestrofacil (3306)         │
│  siniestrofacil_usuarios │ siniestrofacil │ siniestrofacil_polizas │ │
│                          │                │ siniestrofacil_talleres  │
└─────────────────────────────────────────────────────────────────────┘
```

- **Una base de datos por microservicio** (patrón *Database per Service*).
- **ms-usuarios** entrega el token; los otros tres lo validan con la misma clave (`jwt.secret`), sin consultarle en cada petición.
- **Los microservicios no se llaman entre sí.** El registro de la denuncia nunca depende de que Pólizas o Talleres estén disponibles, como exige el caso.

### Flujo de una denuncia

```
1. Asegurado ─► ms-denuncias  POST /denuncias            → folio SF-2026-000001, estado RECIBIDA
                     │
     (EP2: se ejecuta desde Postman con el ADMIN · EP3: Amazon SQS → AWS Lambda)
                     ▼
2. ms-polizas   GET /polizas/{numero}/vigencia
        │ no vigente ─► ms-denuncias PATCH /denuncias/{folio}/resultado → RECHAZADA (no continúa)
        ▼ vigente
3. ms-talleres  POST /asignaciones ─► ms-denuncias PATCH /denuncias/{folio}/resultado → TALLER_ASIGNADO
                     │
4. Taller ─► ms-denuncias  GET /denuncias/asignadas → PATCH /denuncias/{folio}/recepcion → VEHICULO_RECIBIDO
```

---

## 👥 Roles y usuarios de demostración

| Rol | Qué puede hacer |
|---|---|
| **ASEGURADO** | Registrar denuncias y consultar **solo las suyas**; ver sus pólizas |
| **TALLER** | Consultar **únicamente** las denuncias asignadas a su taller y confirmar la recepción del vehículo |
| **ADMIN** | Gestionar usuarios, pólizas y talleres; registrar el resultado del procesamiento (en la EP3 lo hará la Lambda) |

Se crean solos al iniciar cada microservicio (si no existen):

| Rol | Email | Contraseña | Datos asociados |
|---|---|---|---|
| ADMIN | `admin@siniestrofacil.cl` | `Admin123!` | — |
| ASEGURADO | `asegurado@siniestrofacil.cl` | `Asegurado123!` | RUT `12345678-9` · pólizas POL-001 (vigente), POL-002 (vencida), POL-003 (anulada) |
| TALLER | `taller@siniestrofacil.cl` | `Taller123!` | Taller Central (id 1) |

---

## 🛠 Tecnologías

| Tecnología | Versión | Para qué se usa |
|---|---|---|
| Java (OpenJDK) | 25 | Lenguaje de los 4 microservicios |
| Spring Boot | 4.1.1 | Framework base (Web MVC, Validation, Data JPA, Security) |
| jjwt | 0.12.6 | Generar y validar tokens JWT |
| MySQL (contenedor Docker) | 8.4 | Bases de datos de los microservicios |
| Docker Desktop | Última | Contenedor de MySQL |
| springdoc-openapi | 3.1.1 | Swagger UI |
| Lombok | (gestionado por Spring Boot) | Getters, setters y constructores |
| Maven Wrapper | 3.9.x | Compilar, probar y empaquetar sin instalar Maven |
| JUnit 5 + H2 | (incluido) | Pruebas automáticas sin MySQL |
| Postman | Última | Pruebas de los endpoints |
| VS Code | Última | IDE (*Extension Pack for Java*) |

---

## 📂 Estructura del repositorio

```
siniestrofacil/
├── README.md                      ← este archivo
├── .vscode/
│   ├── launch.json                ← levantar los 4 microservicios con F5
│   ├── tasks.json                 ← encender MySQL, crear bases y compilar
│   └── settings.json
├── docker-compose.yml             ← opcional (ver nota en "Solución de problemas")
├── ms-usuarios/                   ← puerto 8080
├── ms-denuncias/                  ← puerto 8081 (incluye la imagen de MySQL en docker/)
├── ms-polizas/                    ← puerto 8082
├── ms-talleres/                   ← puerto 8083
├── postman/
│   ├── ms-usuarios.postman_collection.json
│   ├── ms-denuncias.postman_collection.json
│   ├── ms-polizas.postman_collection.json
│   └── ms-talleres.postman_collection.json
└── docs/capturas/                 ← evidencias
```

Cada microservicio tiene la misma estructura en capas:

```
ms-xxx/
├── pom.xml · mvnw.cmd · DockerfileJar
├── docker/create.sql                         ← crea su base de datos
└── src/main/java/cl/siniestrofacil/xxx/
    ├── controller/   → endpoints REST
    ├── service/      → reglas de negocio
    ├── repository/   → Spring Data JPA
    ├── model/        → entidades JPA (@OneToMany / @ManyToOne)
    ├── dto/          → datos de entrada/salida con validaciones
    ├── security/     → JWT y permisos por rol
    ├── exception/    → GlobalExceptionHandler
    └── config/       → Swagger y datos de demostración
```

---

## ✅ Requisitos previos

| Herramienta | Cómo verificar | Debe mostrar |
|---|---|---|
| **JDK 25** | `java -version` | `openjdk version "25..."` |
| **Git** | `git --version` | Cualquier versión |
| **Docker Desktop** (con WSL 2) | `docker --version` | Y abajo a la izquierda: *Engine running* |
| **Postman** | Abrir la app | — |
| **VS Code** + *Extension Pack for Java* | — | — |

> No hace falta instalar Maven: cada microservicio trae **Maven Wrapper** (`mvnw.cmd`).
> La variable `JAVA_HOME` debe apuntar al **JDK 25**. Verifícalo con `echo $env:JAVA_HOME` y, dentro de cualquier microservicio, con `.\mvnw.cmd -v`.

---

## Paso 1 — Clonar el repositorio

```powershell
git clone https://github.com/Almissin/siniestrofacil.git
cd siniestrofacil
code .
```

En VS Code, espera a que la barra inferior diga **Java: Ready** (importa los 4 proyectos Maven).

---

## Paso 2 — Encender MySQL en Docker

> **Siempre primero.** Ningún microservicio arranca sin MySQL.

### Primera vez en el PC (crear el contenedor)

```powershell
docker network create siniestrofacil-net
cd ms-denuncias\docker
docker build -t mysql-siniestrofacil .
docker run -d --name mysql-siniestrofacil --network siniestrofacil-net -p 3306:3306 mysql-siniestrofacil
cd ..\..
```

### Las siguientes veces (solo encenderlo)

```powershell
docker start mysql-siniestrofacil
```

### Esperar a que esté listo

```powershell
docker logs -f mysql-siniestrofacil
```

Cuando aparezca `ready for connections`, presiona `Ctrl + C`. Confirma con:

```powershell
docker ps
```

Debe aparecer `mysql-siniestrofacil` con estado **Up** y `0.0.0.0:3306->3306/tcp`.

| Dato | Valor |
|---|---|
| Contenedor | `mysql-siniestrofacil` |
| Usuario / contraseña | `sfuser` / `sfpass` |
| Contraseña de root | `root` |
| Puerto | `3306` |

> **Desde VS Code:** *Terminal → Run Task… → "SiniestroFacil: preparar MySQL"* enciende el contenedor, espera a que responda y crea las bases (Pasos 2 y 3 juntos).

---

## Paso 3 — Crear las bases de datos

Desde la raíz del repo (una sola vez; si ya existen, no hace nada):

```powershell
Get-Content ms-usuarios\docker\create.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
Get-Content ms-polizas\docker\create.sql  | docker exec -i mysql-siniestrofacil mysql -uroot -proot
Get-Content ms-talleres\docker\create.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
Get-Content ms-denuncias\docker\migracion-v2.sql | docker exec -i mysql-siniestrofacil mysql -uroot -proot
```

> En PowerShell no funciona `<` para redirigir archivos; por eso se usa `Get-Content ... |`.

Verificar:

```powershell
docker exec -it mysql-siniestrofacil mysql -usfuser -psfpass -e "SHOW DATABASES;"
```

Deben aparecer `siniestrofacil`, `siniestrofacil_usuarios`, `siniestrofacil_polizas` y `siniestrofacil_talleres`.

---

## Paso 4 — Compilar, probar y empaquetar con Maven

En cada microservicio:

```powershell
cd ms-usuarios
.\mvnw.cmd clean package
cd ..
```

| Comando | Qué hace |
|---|---|
| `.\mvnw.cmd clean` | Borra la carpeta `target` anterior |
| `.\mvnw.cmd test` | Ejecuta las pruebas (con H2, no necesitan MySQL) |
| `.\mvnw.cmd package` | Compila, prueba y genera el `.jar` en `target\` |
| `.\mvnw.cmd clean install` | Lo anterior + lo instala en el repositorio local de Maven |

Resultado esperado en cada uno: `BUILD SUCCESS` y el archivo `target\ms-xxx-0.0.1-SNAPSHOT.jar`.

| Microservicio | Pruebas | Archivo generado |
|---|---|---|
| ms-usuarios | 5 | `target\ms-usuarios-0.0.1-SNAPSHOT.jar` |
| ms-denuncias | 6 | `target\ms-denuncias-0.0.1-SNAPSHOT.jar` |
| ms-polizas | 6 | `target\ms-polizas-0.0.1-SNAPSHOT.jar` |
| ms-talleres | 6 | `target\ms-talleres-0.0.1-SNAPSHOT.jar` |

> **Desde VS Code:** *Terminal → Run Task… → "SiniestroFacil: compilar los 4 con Java 25"* compila los 4 seguidos.

---

## Paso 5 — Levantar los microservicios

> **Antes:** MySQL encendido (Paso 2). **Orden:** ms-usuarios → ms-denuncias → ms-polizas → ms-talleres. Espera a que cada uno muestre `Tomcat started on port ...` antes de iniciar el siguiente.

### Opción A — VS Code con un clic (recomendada para trabajar)

1. Panel **Run and Debug** (`Ctrl + Shift + D`).
2. Elegir **"SiniestroFacil: levantar todo"**.
3. Presionar **F5**.

Enciende MySQL, crea las bases si faltan y levanta los 4 microservicios, cada uno en su propia terminal. Para detenerlos todos: **Shift + F5**.

### Opción B — Terminal (una terminal por microservicio)

| Terminal | Comando | Debe aparecer |
|---|---|---|
| 1 | `cd ms-usuarios; .\mvnw.cmd spring-boot:run` | `Tomcat started on port 8080` |
| 2 | `cd ms-denuncias; .\mvnw.cmd spring-boot:run` | `Tomcat started on port 8081` |
| 3 | `cd ms-polizas; .\mvnw.cmd spring-boot:run` | `Tomcat started on port 8082` |
| 4 | `cd ms-talleres; .\mvnw.cmd spring-boot:run` | `Tomcat started on port 8083` |

### Opción C — Ejecutar el `.jar` generado en el Paso 4

```powershell
java -jar ms-usuarios\target\ms-usuarios-0.0.1-SNAPSHOT.jar
```

(Igual para los otros tres, cada uno en su terminal.)

---

## Paso 6 — Verificar en Swagger

| Microservicio | Swagger UI | Especificación OpenAPI |
|---|---|---|
| ms-usuarios | http://localhost:8080/ | http://localhost:8080/v3/api-docs |
| ms-denuncias | http://localhost:8081/ | http://localhost:8081/v3/api-docs |
| ms-polizas | http://localhost:8082/ | http://localhost:8082/v3/api-docs |
| ms-talleres | http://localhost:8083/ | http://localhost:8083/v3/api-docs |

**Cómo usar el token:**

1. En http://localhost:8080/ → `POST /auth/login` → **Try it out** → **Execute** (el ejemplo ya trae al admin).
2. Copiar el valor de `token` de la respuesta.
3. En cualquier Swagger presionar **Authorize** 🔒, pegar el token (sin la palabra `Bearer`) y presionar **Authorize**.
4. Probar, por ejemplo, `GET /polizas/POL-001/vigencia` en http://localhost:8082/.

---

## Paso 7 — Verificar los datos en MySQL

Cada consulta muestra la **relación JPA** de ese microservicio (`@OneToMany` / `@ManyToOne`):

```powershell
# ms-usuarios: usuarios con su rol (Rol 1:N Usuario)
docker exec -it mysql-siniestrofacil mysql -usfuser -psfpass -t -e "SELECT u.id, u.email, r.nombre AS rol, u.taller_id, u.activo FROM siniestrofacil_usuarios.usuarios u JOIN siniestrofacil_usuarios.roles r ON r.id = u.rol_id;"

# ms-polizas: polizas con sus coberturas (Poliza 1:N Cobertura)
docker exec -it mysql-siniestrofacil mysql -usfuser -psfpass -t -e "SELECT p.numero, p.fecha_termino, p.activa, c.nombre AS cobertura, c.monto_maximo FROM siniestrofacil_polizas.polizas p JOIN siniestrofacil_polizas.coberturas c ON c.poliza_id = p.id;"

# ms-talleres: talleres y sus asignaciones (Taller 1:N Asignacion)
docker exec -it mysql-siniestrofacil mysql -usfuser -psfpass -t -e "SELECT t.id, t.nombre, t.cupo_maximo, a.folio_denuncia, a.estado FROM siniestrofacil_talleres.talleres t LEFT JOIN siniestrofacil_talleres.asignaciones a ON a.taller_id = t.id;"

# ms-denuncias: denuncias con su estado y fotografias (Denuncia 1:N Fotografia)
docker exec -it mysql-siniestrofacil mysql -usfuser -psfpass -t -e "SELECT d.folio, d.numero_poliza, d.estado, d.nombre_taller, COUNT(f.id) AS fotos FROM siniestrofacil.denuncias d LEFT JOIN siniestrofacil.fotografias f ON f.denuncia_folio = d.folio GROUP BY d.folio;"
```

> Para ver el **CRUD reflejado en la base de datos**, ejecuta la consulta antes y después de una operación en Postman (por ejemplo, registrar una denuncia o crear una póliza).

---

## Paso 8 — Pruebas con Postman

Importar las 4 colecciones de la carpeta `postman/` (**Import** → seleccionar los archivos) y ejecutarlas con el **Collection Runner**, en el orden por defecto:

| Orden | Colección | Requiere | Pruebas |
|---|---|---|---|
| 1 | `ms-usuarios.postman_collection.json` | ms-usuarios | 31 |
| 2 | `ms-polizas.postman_collection.json` | ms-usuarios + ms-polizas | 27 |
| 3 | `ms-talleres.postman_collection.json` | ms-usuarios + ms-talleres | 31 |
| 4 | `ms-denuncias.postman_collection.json` | **los 4** | 41 |

Cada colección hace primero el login y guarda los tokens sola. Cubren respuestas exitosas (`200`, `201`, `204`) y de error (`400`, `401`, `403`, `404`, `409`).

La **carpeta 4 de ms-denuncias** recorre el flujo completo del caso, simulando lo que hará la AWS Lambda en la EP3:

1. Asegurado registra 2 denuncias (POL-001 y POL-002) → `RECIBIDA`.
2. POL-002 está **vencida** → la denuncia queda `RECHAZADA` (no continúa a la asignación).
3. POL-001 está **vigente** → ms-talleres asigna taller → `TALLER_ASIGNADO`.
4. El taller ve sus denuncias asignadas y confirma la recepción → `VEHICULO_RECIBIDO`.

---

## ⏹ Apagar todo

1. Detener los microservicios: **Shift + F5** en VS Code (si usaste F5) o `Ctrl + C` en cada terminal.
2. Detener MySQL:

```powershell
docker stop mysql-siniestrofacil
```

Los datos **no se pierden**: quedan guardados en el contenedor para la próxima vez.

---

## 📸 Evidencias

Capturas tomadas con los 4 microservicios corriendo en local.

### MySQL y Maven

**1. Contenedor `mysql-siniestrofacil` corriendo (`docker ps`)**

![docker ps](docs/capturas/ep2/01-docker-mysql-up.png)

**2. Las 4 bases de datos creadas (`SHOW DATABASES`)**

![Bases de datos](docs/capturas/ep2/02-mysql-bases.png)

**3. Compilación con Maven — `BUILD SUCCESS` y pruebas aprobadas**

![Maven build](docs/capturas/ep2/03-maven-build-success.png)

**4. Los 4 microservicios levantados en VS Code**

![VS Code](docs/capturas/ep2/04-vscode-4-microservicios.png)

### Swagger

**5. ms-usuarios — login y token JWT**

![Swagger ms-usuarios](docs/capturas/ep2/05-swagger-usuarios-login.png)

**6. ms-denuncias — denuncia registrada con folio (201)**

![Swagger ms-denuncias](docs/capturas/ep2/06-swagger-denuncias-201.png)

**7. ms-polizas — vigencia de una póliza**

![Swagger ms-polizas](docs/capturas/ep2/07-swagger-polizas-vigencia.png)

**8. ms-talleres — taller asignado a una denuncia (201)**

![Swagger ms-talleres](docs/capturas/ep2/08-swagger-talleres-asignacion.png)

### Datos en MySQL

**9. Usuarios con su rol**

![SQL usuarios](docs/capturas/ep2/09-sql-usuarios-roles.png)

**10. Pólizas con sus coberturas**

![SQL polizas](docs/capturas/ep2/10-sql-polizas-coberturas.png)

**11. Talleres con sus asignaciones**

![SQL talleres](docs/capturas/ep2/11-sql-talleres-asignaciones.png)

**12. Denuncias con estado, taller y fotografías**

![SQL denuncias](docs/capturas/ep2/12-sql-denuncias-estados.png)

### Postman

**13. Collection Runner — flujo completo de ms-denuncias aprobado**

![Postman runner](docs/capturas/ep2/13-postman-runner-denuncias.png)

> Las evidencias de la versión 1 de ms-denuncias (Swagger, Postman y Docker) están en [`docs/capturas/`](docs/capturas/).

---

## 🩺 Solución de problemas

| Problema | Causa probable | Solución |
|---|---|---|
| `Communications link failure` / `Connection refused` al iniciar | MySQL apagado o iniciando | `docker start mysql-siniestrofacil`, esperar `ready for connections` y volver a iniciar el microservicio |
| `Unknown database 'siniestrofacil_...'` | No se crearon las bases | Ejecutar el **Paso 3** |
| `Data truncated for column 'estado'` (ms-denuncias) | Falta la migración v2 | Ejecutar `migracion-v2.sql` (Paso 3) |
| `Port 808X was already in use` | Otro proceso o contenedor usa el puerto | Ver la nota de `docker-compose` abajo, o `Get-NetTCPConnection -LocalPort 808X -State Listen` y `Stop-Process -Id <numero> -Force` |
| `Bind for 0.0.0.0:3306 failed: port is already allocated` | Otro MySQL (de Windows u otro proyecto) usa el 3306 | Detener ese servicio o contenedor y repetir el Paso 2 |
| `release version 25 not supported` | Maven usa un JDK antiguo | `JAVA_HOME` debe apuntar al JDK 25; cerrar y abrir VS Code |
| `401` en todas las peticiones | Falta el token o venció (dura 2 horas) | Repetir el login y usar **Authorize** |
| `403` | El rol no tiene permiso para esa operación | Usar el usuario del rol correcto (ver tabla de roles) |
| Errores rojos de Lombok en VS Code | VS Code no procesó el proyecto | `Ctrl + Shift + P` → `Java: Clean Java Language Server Workspace` |

> **Nota sobre `docker-compose.yml`:** es una alternativa opcional que levanta MySQL y los 4 microservicios en contenedores. Sus contenedores tienen `restart: unless-stopped`, así que **se encienden solos al abrir Docker Desktop** y ocupan los puertos 8080–8083. Si vas a trabajar desde VS Code, déjalos apagados:
>
> ```powershell
> docker stop ms-usuarios ms-denuncias ms-polizas ms-talleres
> ```
>
> Deja encendido solo `mysql-siniestrofacil`.

---

## ☁️ Próxima etapa: AWS (EP3)

En la tercera experiencia se integrarán **funciones serverless en AWS** y los microservicios se desplegarán en la nube. Según el diseño de la EP1:

| Hoy (EP2, local y manual) | EP3 (AWS) |
|---|---|
| Postman llama directo a cada puerto | **Amazon API Gateway** como única entrada; los microservicios no se exponen directamente |
| El ADMIN ejecuta desde Postman la validación de póliza y la asignación de taller | **Amazon SQS** encola las denuncias nuevas y una **AWS Lambda** las procesa sola (con cola de errores DLQ) |
| MySQL y microservicios se encienden a mano | Despliegue en la nube; se espera que quede automatizado |

Por eso los endpoints `GET /polizas/{numero}/vigencia`, `POST /asignaciones` y `PATCH /denuncias/{folio}/resultado` ya existen: son los que llamará la Lambda.

---

## 👥 Equipo

| Integrante |
|---|
| Roberto Bustamante |
| Alex Messin De La Cruz |
| Jahaira Torrijo |

**DUOC UC — Escuela de Informática y Telecomunicaciones**
Asignatura JVY0101 · 2026
