# imagen optimizada de ms-usuarios con construccion en dos etapas
# construir desde la carpeta ms-usuarios con docker build -t ms-usuarios .

# ---- etapa de compilacion con el jdk completo ----
FROM eclipse-temurin:25-jdk-alpine AS compilacion
WORKDIR /fuente

# copiar primero el wrapper y el pom para cachear las dependencias en una capa propia
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# copiar el codigo y empaquetar, las pruebas ya corren en el pipeline de ci
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests && cp target/*-SNAPSHOT.jar app.jar

# ---- etapa de ejecucion solo con el jre sobre alpine ----
FROM eclipse-temurin:25-jre-alpine

# instalar los datos de zona horaria para usar la hora de chile
RUN apk add --no-cache tzdata
ENV TZ=America/Santiago

# crear un usuario sin privilegios para no ejecutar la aplicacion como root
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=compilacion --chown=app:app /fuente/app.jar app.jar
USER app

EXPOSE 8080

# revisar que la aplicacion responda usando la documentacion publica de springdoc
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD wget -q -O /dev/null http://localhost:8080/v3/api-docs || exit 1

# limitar la memoria de la jvm al limite del contenedor
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
