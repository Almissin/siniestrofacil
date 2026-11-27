# desplegar o actualizar el stack siniestrofacil en el swarm

# seguir aunque docker escriba avisos en la salida de error y validar con LASTEXITCODE
$ErrorActionPreference = "Continue"
$raiz = Split-Path -Parent $PSScriptRoot

# bajar los contenedores de docker compose para liberar los puertos 3306 y 8080 a 8083
Push-Location $raiz
docker compose down
Pop-Location

# desplegar el stack, si ya existe solo aplica los cambios
docker stack deploy -c (Join-Path $PSScriptRoot "docker-stack.yml") siniestrofacil

Write-Host ""
Write-Host "Stack desplegado. Los microservicios tardan 1 a 2 minutos en quedar listos."
Write-Host "Revisa el estado con: docker stack services siniestrofacil"
docker stack services siniestrofacil
