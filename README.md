# 🚗 SiniestroFácil — Microservicios (EP2)

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger)
![Postman](https://img.shields.io/badge/Postman-Pruebas-FF6C37?logo=postman)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-MySQL-2496ED?logo=docker&logoColor=white)

**SiniestroFácil** es una plataforma para que los asegurados **denuncien siniestros automotrices**. El asegurado registra la denuncia y recibe de inmediato un **folio de recepción**. Después, en un proceso independiente, se **valida la póliza** y, si está vigente, se **asigna un taller en convenio**, que luego confirma la recepción del vehículo.

El sistema está formado por **4 microservicios Spring Boot**, cada uno con su **propia base de datos** en un contenedor MySQL.

---

> ## ⚠️ NOTA IMPORTANTE — Orden de encendido
>
> En esta etapa **todo se levanta de forma manual y en este orden**:
>
> 1. **Docker Desktop** abierto y con *Engine running*.
> 2. **El contenedor `mysql-siniestrofacil`** encendido (`docker start mysql-siniestrofacil`) y esperando `ready for connections`.
> 3. **Los microservicios**, desde **VS Code** (o la terminal / IntelliJ), en este orden:
>    **ms-usuarios (8080) → ms-denuncias (8081) → ms-polizas (8082) → ms-talleres (8083)**.
>
> Si un microservicio se inicia **antes** que MySQL, falla con `Communications link failure`. Basta con encender MySQL y volver a iniciarlo.
>
> **Por qué es manual:** la evaluación pide ejecutar los microservicios **en un ambiente local** con **Maven**. En la siguiente etapa (Unidad 3) el proyecto se llevará a **AWS** (API Gateway, Amazon SQS y AWS Lambda), donde se espera que el despliegue y el procesamiento de las denuncias queden **automatizados en la nube**.

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
15. [Docker Swarm (EP3)](#-docker-swarm-ep3)
16. [Apagar todo](#-apagar-todo)
17. [Evidencias](#-evidencias)
18. [Solución de problemas](#-solución-de-problemas)
19. [Serverless en AWS (EP3)](#️-serverless-en-aws-ep3)
20. [Equipo](#-equipo)

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

## 🐳 Docker Swarm (EP3)

El stack `swarm/docker-stack.yml` despliega MySQL y los cuatro microservicios como **servicios de Swarm**, con réplicas, balanceo de carga, reinicio automático y actualizaciones sin corte. La demo corre en **un solo equipo**, que cumple los roles de manager y worker.

### Archivos

| Archivo | Para qué sirve |
| --- | --- |
| `swarm/docker-stack.yml` | Definición del stack: servicios, réplicas, red overlay, secreto JWT, límites de recursos |
| `swarm/01-iniciar-swarm.ps1` | Ejecuta `docker swarm init` y crea el secreto `jwt_secret` |
| `swarm/02-construir-imagenes.ps1` | Compila los 4 `.jar` y construye las imágenes con `docker compose build` |
| `swarm/03-desplegar-stack.ps1` | Baja el `docker compose` local y ejecuta `docker stack deploy` |
| `swarm/04-escalar.ps1` | Cambia las réplicas de un microservicio |
| `swarm/05-eliminar-stack.ps1` | Elimina el stack y, opcionalmente, sale del Swarm |
| `swarm/agregar-nodo.md` | Cómo sumar nodos manager y worker, y qué puertos abrir |

### Desplegar

Con Docker Desktop encendido, desde la raíz del repositorio:

```powershell
powershell -ExecutionPolicy Bypass -File swarm\01-iniciar-swarm.ps1
powershell -ExecutionPolicy Bypass -File swarm\02-construir-imagenes.ps1
powershell -ExecutionPolicy Bypass -File swarm\03-desplegar-stack.ps1
```

Los microservicios tardan 1 a 2 minutos en quedar listos. Mientras MySQL arranca, Swarm reinicia las réplicas que fallan al conectarse a la base, hasta que quedan arriba. Para revisar:

```powershell
docker stack services siniestrofacil        # REPLICAS debe mostrar 2/2 o 1/1
docker service ps siniestrofacil_ms-denuncias
docker service logs -f siniestrofacil_ms-denuncias
```

Los endpoints quedan en los mismos puertos de siempre, con Swagger en `http://localhost:8080` a `8083`. Swarm reparte cada petición entre las réplicas del servicio usando su *routing mesh*.

### Escalar

```powershell
powershell -ExecutionPolicy Bypass -File swarm\04-escalar.ps1 -Servicio ms-denuncias -Replicas 4
powershell -ExecutionPolicy Bypass -File swarm\04-escalar.ps1 -Servicio ms-denuncias -Replicas 2
```

Es lo mismo que `docker service scale siniestrofacil_ms-denuncias=4`.

### Agregar nodos

```powershell
docker swarm join-token worker    # comando para sumar un worker
docker swarm join-token manager   # comando para sumar un manager
docker node ls                    # nodos del cluster
```

El detalle de puertos, registro de imágenes y cómo retirar un nodo está en [`swarm/agregar-nodo.md`](swarm/agregar-nodo.md).

### Decisiones técnicas

| Decisión | Por qué |
| --- | --- |
| 2 réplicas de `ms-usuarios` y `ms-denuncias`, 1 de `ms-polizas` y `ms-talleres` | Login y denuncias son la puerta de entrada y el flujo con más tráfico. Si cae una réplica, la otra sigue atendiendo |
| MySQL con 1 réplica fija en el manager (`node.role == manager`) | El volumen `datos-mysql` es local a ese nodo. Una base de datos no se escala con réplicas sin replicación propia |
| Red `overlay` | Conecta los servicios por nombre, por ejemplo `mysql:3306`, aunque estén en nodos distintos |
| `restart_policy: on-failure` con 10 intentos | Swarm ignora `depends_on`. Si un microservicio arranca antes que MySQL, falla y Swarm lo reintenta |
| `update_config` con `parallelism: 1` y `order: start-first` | Actualiza de a una réplica y levanta la nueva antes de bajar la antigua, sin corte de servicio |
| `failure_action: rollback` | Si la nueva versión falla al arrancar, Swarm vuelve sola a la anterior |
| Límites de 512 MB y 1 CPU por réplica, con `MaxRAMPercentage=75` | Una réplica con fuga de memoria no puede tumbar al resto, y la JVM respeta el límite del contenedor |
| La clave JWT como **Docker secret** | No queda en el YAML ni en variables de entorno. Se monta en `/run/secrets/JWT_SECRET` y Spring la lee con `configtree` |
| Scripts de MySQL como **Docker configs** | Swarm distribuye los `create.sql` a cualquier nodo, sin depender de rutas del equipo |

**Pendiente si el clúster crece:** subir las imágenes a un registro, mover también las credenciales de MySQL a secretos y agregar `HEALTHCHECK` con Spring Actuator para que Swarm sepa cuándo una réplica está lista y no solo encendida.

### Apagar el stack

```powershell
powershell -ExecutionPolicy Bypass -File swarm\05-eliminar-stack.ps1                  # quita el stack
powershell -ExecutionPolicy Bypass -File swarm\05-eliminar-stack.ps1 -SalirDelSwarm   # y además deja el Swarm
```

Los datos quedan en el volumen `siniestrofacil_datos-mysql`.

---

## ⏹ Apagar todo

1. Detener los microservicios: **Shift + F5** en VS Code (si usaste F5) o `Ctrl + C` en cada terminal.
2. Detener MySQL:

```powershell
docker stop mysql-siniestrofacil
```

Los datos **no se pierden**: quedan guardados en el contenedor para la próxima vez.

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


---

## ☁️ Serverless en AWS (EP3)

La parte en la nube implementa la decisión central del proyecto: **el ingreso de la denuncia es síncrono y entrega el folio al instante, y la validación de póliza y la asignación de taller ocurren después, de forma asíncrona**, a través de una cola.

```
 Cliente ──► API Gateway (HTTP API) ──► Lambda autorizador ── JWT inválido ──► 401 / 403
              throttling 10 req/s          │ JWT válido
                                           ▼
                                  ms-denuncias (Lambda) ── 201 + folio RECIBIDA ──► Cliente
                                           │ publica
                                           ▼
                                SQS siniestrofacil-denuncias  (espera 10 s)
                                           │ falla 3 veces ──► SQS siniestrofacil-denuncias-errores
                                           ▼
                                Lambda procesarDenuncia
                                  valida póliza y asigna taller
                                           │ PATCH /denuncias/{folio}/resultado
                                           ▼
                                ms-denuncias ── estado TALLER_ASIGNADO o RECHAZADA
```

| Componente | Dónde está | Qué hace |
| --- | --- | --- |
| HTTP API Gateway | `ms-denuncias/serverless.yml` | Expone solo `/denuncias` y Swagger, exige JWT y limita a 10 peticiones por segundo |
| `autorizador` | `lambda-denuncias/.../AutorizadorHandler.java` | Valida firma, emisor y vencimiento del JWT antes de llegar al backend |
| `api` | `ms-denuncias` + Lambda Web Adapter | El mismo Spring Boot de siempre. Registra la denuncia y publica el mensaje en SQS (`PublicadorDenuncias`) |
| `siniestrofacil-denuncias` | Cola SQS | Desacopla el ingreso del procesamiento. Entrega cada mensaje 10 s después, para que se vea el estado `RECIBIDA` |
| `siniestrofacil-denuncias-errores` | Cola SQS (DLQ) | Guarda los mensajes que fallan 3 veces |
| `procesarDenuncia` | `lambda-denuncias/.../ProcesarDenunciaHandler.java` | Valida la póliza (`ValidadorPoliza`) y guarda el resultado en ms-denuncias |

### Desplegar

No se despliega a mano. Cualquier push a `main` que toque `ms-denuncias/` o `lambda-denuncias/` ejecuta `.github/workflows/deploy-ms-denuncias.yml`:

1. **Integración continua:** pruebas de ms-denuncias, `.jar` y `.zip` para la Lambda, pruebas y `.jar` de `lambda-denuncias`.
2. **Despliegue continuo:** `serverless deploy` crea o actualiza en CloudFormation el API Gateway, el autorizador, las tres Lambdas y las dos colas.

Requiere los secrets `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN` del Learner Lab. Hay que actualizarlos cada vez que se reinicia el lab.

### Probar

1. Obtén un token del asegurado en ms-usuarios, el que corre en Docker local o Swarm. La clave JWT es la misma, así que el token sirve en AWS:
   `POST http://localhost:8080/auth/login` con `{"email":"asegurado@siniestrofacil.cl","password":"Asegurado123!"}`
2. Registra una denuncia en AWS con ese token:
   `POST https://<api-id>.execute-api.us-east-1.amazonaws.com/denuncias`
   ```json
   {"patente":"ABCD12","numeroPoliza":"POL-001","fechaSiniestro":"2026-11-20","descripcion":"choque por alcance"}
   ```
   Responde **201** con el folio y estado `RECIBIDA`.
3. Espera unos 15 segundos y consulta `GET .../denuncias/{folio}`. El estado pasa a **`TALLER_ASIGNADO`** con *Taller Central*.
4. Con `POL-002` o `POL-003` la denuncia termina **`RECHAZADA`** con el motivo.
5. Sin token, el API Gateway responde **401** y la petición nunca llega a ms-denuncias.
6. Los logs están en CloudWatch: `/aws/lambda/siniestrofacil-ms-denuncias-dev-procesarDenuncia` y `-autorizador`.

| Póliza | Patente | Resultado esperado |
| --- | --- | --- |
| POL-001 | ABCD12 | `TALLER_ASIGNADO` |
| POL-002 | AB1234 | `RECHAZADA`, no estaba vigente |
| POL-003 | BCDF34 | `RECHAZADA`, póliza anulada |

### Decisiones y limitaciones

- **Las reglas de pólizas van dentro de la Lambda.** ms-polizas y ms-talleres corren en Docker local, y una Lambda en AWS no puede alcanzarlos. `ValidadorPoliza` replica las pólizas de demostración. El siguiente paso sería desplegar esos microservicios en la nube y que la Lambda los consulte.
- **ms-denuncias usa H2 en memoria en AWS.** Los datos se pierden cuando la Lambda se reinicia. Para la demo se trabaja con peticiones en secuencia, para que las atienda la misma instancia. En producción se usaría Amazon RDS.
- **Doble validación del JWT.** El autorizador filtra en el borde y ms-denuncias vuelve a validar el rol de cada endpoint. Si se salta una capa, la otra protege.

---

## 👥 Equipo

| Integrante |
|---|
| Roberto Bustamante |
| Alex Messin De La Cruz |

**DUOC UC — Escuela de Informática y Telecomunicaciones**
Asignatura JVY0101 · 2026
