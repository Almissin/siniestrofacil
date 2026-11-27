# construir las imagenes docker de los cuatro microservicios y de mysql
# los Dockerfile compilan el jar adentro en dos etapas, no hace falta maven en el equipo
# swarm no construye imagenes, solo despliega las que ya existen

# seguir aunque docker escriba avisos en la salida de error y validar con LASTEXITCODE
$ErrorActionPreference = "Continue"
$raiz = Split-Path -Parent $PSScriptRoot

# construir usando las secciones build del docker-compose.yml
Push-Location $raiz
docker compose build
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Fallo docker compose build" }
Pop-Location

# mostrar las imagenes construidas con su tamano
docker image ls --filter "reference=ms-*" --filter "reference=mysql-siniestrofacil"
