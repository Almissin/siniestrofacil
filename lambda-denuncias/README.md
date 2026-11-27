# lambda-denuncias — Funciones serverless de SiniestroFácil (EP3)

Este módulo contiene las dos funciones AWS Lambda que completan el flujo de denuncias en la nube:

| Función | Handler | Disparador | Qué hace |
|---|---|---|---|
| `autorizador` | `cl.siniestrofacil.lambda.AutorizadorHandler::handleRequest` | API Gateway (autorizador de solicitud) | Valida el JWT emitido por ms-usuarios antes de que la petición llegue a ms-denuncias |
| `procesarDenuncia` | `cl.siniestrofacil.lambda.ProcesarDenunciaHandler::handleRequest` | Cola SQS `siniestrofacil-denuncias` | Valida la póliza, asigna taller o rechaza la denuncia, y guarda el resultado en ms-denuncias |

La tercera función, `api`, es el propio ms-denuncias (Spring Boot) ejecutado con Lambda Web Adapter. Su configuración está en [`../ms-denuncias/serverless.yml`](../ms-denuncias/serverless.yml).

---

## 1. Estructura

```
lambda-denuncias/
├── pom.xml                                   ← build con Maven; genera target/lambda-denuncias.jar
├── README.md                                 ← este archivo
└── src/
    ├── main/java/cl/siniestrofacil/lambda/
    │   ├── AutorizadorHandler.java           ← autorizador del API Gateway
    │   ├── ProcesarDenunciaHandler.java      ← consumidor de la cola SQS
    │   ├── ValidadorPoliza.java              ← reglas de negocio de pólizas y talleres
    │   ├── Jwt.java                          ← validación y creación de JWT (HS256)
    │   └── Config.java                       ← lectura de variables de entorno
    └── test/java/cl/siniestrofacil/lambda/
        ├── AutorizadorHandlerTest.java
        ├── JwtTest.java
        └── ValidadorPolizaTest.java
```

Java 21 (`maven.compiler.release` = 21), Maven, AWS Lambda Java Core 1.2.3 y Jackson 2.18.2. El `.jar` se genera con maven-shade-plugin para incluir las dependencias.

---

## 2. Variables de entorno

| Variable | Obligatoria | Función | Valor en AWS |
|---|---|---|---|
| `JWT_SECRET` | No (tiene valor por defecto de desarrollo) | Clave HS256 para validar y firmar tokens | Igual a la de ms-usuarios y ms-denuncias |
| `API_URL` | Sí, en `procesarDenuncia` | URL base del API Gateway, por ejemplo `https://<api-id>.execute-api.us-east-1.amazonaws.com` | Calculada por `serverless.yml` a partir de `HttpApi` |

`serverless.yml` define `API_URL` automáticamente. Si falta, `procesarDenuncia` lanza `IllegalStateException` y el mensaje vuelve a la cola.

---

## 3. Función `autorizador`

Recibe el evento de API Gateway (formato de payload 2.0) y responde con una estructura simple:

```json
{ "isAuthorized": true, "context": { "usuario": "asegurado@siniestrofacil.cl", "rol": "ASEGURADO" } }
```

Pasos:
1. Lee el header `Authorization` (sin distinguir mayúsculas) y quita el prefijo `Bearer `.
2. Valida firma HS256, emisor, y vencimiento del token (`Jwt.validar`).
3. Si el token es válido, devuelve `isAuthorized: true` con el usuario y el rol en `context`. Si no, devuelve `false`, y API Gateway responde **401** o **403**.

La respuesta se cachea 30 segundos (`resultTtlInSeconds: 30` en `serverless.yml`).

---

## 4. Función `procesarDenuncia`

Consume mensajes de SQS, con `batchSize: 1` y `maximumConcurrency: 2`. Cada mensaje contiene los datos de una denuncia:

```json
{ "folio": "SF-2026-000001", "numeroPoliza": "POL-001", "patente": "ABCD12", "fechaSiniestro": "2026-11-20" }
```

### Flujo

1. Lee `folio`, `numeroPoliza`, `patente` y `fechaSiniestro`.
2. Llama a `ValidadorPoliza.evaluar(...)`, que decide el resultado.
3. Envía `PATCH {API_URL}/denuncias/{folio}/resultado` con un token de corta duración (300 s) firmado con `JWT_SECRET` y rol `ADMIN`.
4. Interpreta la respuesta:

| Código de ms-denuncias | Significado | Acción |
|---|---|---|
| 200 | Resultado guardado | Termina bien y SQS elimina el mensaje |
| 409 | La denuncia ya estaba procesada | Se descarta el mensaje repetido |
| Otro | Error | Se lanza excepción, SQS reintenta, y tras 3 intentos el mensaje pasa a `siniestrofacil-denuncias-errores` |

### Reglas de negocio (`ValidadorPoliza`)

Se evalúan en este orden. La primera regla que falla determina el rechazo:

| # | Condición | Resultado |
|---|---|---|
| 1 | La póliza no existe | `RECHAZADA`, "la poliza ... no existe" |
| 2 | La póliza está anulada | `RECHAZADA`, "esta anulada" |
| 3 | La patente no corresponde a la póliza | `RECHAZADA`, "no corresponde a la poliza" |
| 4 | La fecha del siniestro está fuera de la vigencia (inicio ≤ fecha < término) | `RECHAZADA`, "no estaba vigente" |
| 5 | Todo lo anterior se cumple | `TALLER_ASIGNADO`, Taller Central (id 1) |

Pólizas de demostración:

| Póliza | Patente | Vigencia | Activa | Resultado esperado |
|---|---|---|---|---|
| POL-001 | ABCD12 | 2026-01-01 a 2027-01-01 | Sí | `TALLER_ASIGNADO` |
| POL-002 | AB1234 | 2024-06-01 a 2025-06-01 | Sí | `RECHAZADA` (no vigente) |
| POL-003 | BCDF34 | 2026-01-01 a 2027-01-01 | No | `RECHAZADA` (anulada) |

---

## 5. Compilar y probar

Desde esta carpeta (requiere JDK 21 o superior compatible y Maven):

```powershell
mvn -B test        # ejecuta las pruebas JUnit 5 (no necesitan AWS ni MySQL)
mvn -B package     # genera target\lambda-denuncias.jar
```

Resultado esperado: `BUILD SUCCESS`. El pipeline de GitHub Actions ejecuta estos mismos comandos en el job 1 (integración continua).

| Prueba | Qué verifica |
|---|---|
| `AutorizadorHandlerTest` | Token válido, token ausente, firma inválida y token vencido |
| `JwtTest` | Creación y validación de JWT |
| `ValidadorPolizaTest` | Las cinco reglas de negocio y el orden en que se aplican |

---

## 6. Despliegue

No se despliega a mano. Un push a `main` que modifique `lambda-denuncias/` o `ms-denuncias/` ejecuta [`.github/workflows/deploy-ms-denuncias.yml`](../.github/workflows/deploy-ms-denuncias.yml):

1. **Integración continua:** `mvn -B package` en esta carpeta, y copia del `.jar` junto al artefacto de ms-denuncias.
2. **Despliegue continuo:** `serverless deploy --stage dev` crea o actualiza el autorizador, `procesarDenuncia`, la cola principal y la cola de errores.

Los secrets `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN` del Learner Lab deben actualizarse cada vez que se reinicia el laboratorio.

---

## 7. Probar en AWS

1. Obtén un token del asegurado en ms-usuarios (local o Swarm):
   `POST http://localhost:8080/auth/login` con `{"email":"asegurado@siniestrofacil.cl","password":"Asegurado123!"}`
2. Registra una denuncia en AWS:
   `POST {API_URL}/denuncias` con header `Authorization: Bearer <token>` y el cuerpo:
   ```json
   {"patente":"ABCD12","numeroPoliza":"POL-001","fechaSiniestro":"2026-11-20","descripcion":"choque por alcance"}
   ```
   Responde **201** con el folio y estado `RECIBIDA`.
3. Espera unos 15 segundos (10 s de retraso en SQS más el procesamiento) y consulta `GET {API_URL}/denuncias/{folio}`. El estado debe ser `TALLER_ASIGNADO`.
4. Sin token, API Gateway responde **401**. Con un token inválido, responde **403**.
5. Revisa los logs en CloudWatch:
   - `/aws/lambda/siniestrofacil-ms-denuncias-dev-autorizador`
   - `/aws/lambda/siniestrofacil-ms-denuncias-dev-procesarDenuncia`

   Los mensajes clave son `[AUTORIZADOR] acceso permitido`, `[CONSUMIDOR] mensaje recibido desde sqs` y `[CONSUMIDOR] folio ... actualizado en ms-denuncias`.

---

## 8. Decisiones y limitaciones

- **Reglas de pólizas dentro de la Lambda.** ms-polizas y ms-talleres corren en Docker local, y una Lambda en AWS no puede alcanzarlos. `ValidadorPoliza` replica las pólizas y el taller de demostración. Esto significa que hay dos fuentes de datos que deben mantenerse iguales. El siguiente paso sería desplegar esos servicios en la nube y consultarlos desde la Lambda.
- **Idempotencia.** SQS entrega al menos una vez, así que un mismo mensaje puede llegar dos veces. El código de respuesta 409 de ms-denuncias evita procesarlo dos veces.
- **Reintentos y cola de errores.** `maxReceiveCount: 3` en la cola principal. Los mensajes que fallan tres veces quedan en la cola de errores para revisión, sin bloquear al resto.
- **Concurrencia limitada.** `maximumConcurrency: 2` porque ms-denuncias usa H2 en memoria por instancia en AWS.
- **Clave JWT de desarrollo.** Si `JWT_SECRET` no se define, se usa un valor por defecto. En producción debe provenir de un secreto externo, por ejemplo AWS Secrets Manager.
- **Token de servicio.** `procesarDenuncia` firma su propio token de rol ADMIN, válido 300 segundos, solo para llamar a ms-denuncias.
