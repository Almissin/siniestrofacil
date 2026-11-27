# compilar los cuatro microservicios y construir sus imagenes docker
# swarm no construye imagenes, solo despliega las que ya existen

# seguir aunque docker escriba avisos en la salida de error y validar con LASTEXITCODE
$ErrorActionPreference = "Continue"
$raiz = Split-Path -Parent $PSScriptRoot

# compilar el jar de cada microservicio sin ejecutar los tests
foreach ($ms in "ms-usuarios", "ms-denuncias", "ms-polizas", "ms-talleres") {
    Write-Host "Compilando $ms..."
    Push-Location (Join-Path $raiz $ms)
    & .\mvnw.cmd -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Fallo la compilacion de $ms" }
    Pop-Location
}

# construir las imagenes usando las secciones build del docker-compose.yml
Push-Location $raiz
docker compose build
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Fallo docker compose build" }
Pop-Location

# mostrar las imagenes construidas
docker image ls --filter "reference=ms-*" --filter "reference=mysql-siniestrofacil"
