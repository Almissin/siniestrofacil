# iniciar docker swarm en este equipo y crear el secreto jwt compartido
# ejecutar desde la raiz del repositorio con
#   powershell -ExecutionPolicy Bypass -File swarm\01-iniciar-swarm.ps1

# seguir aunque docker escriba avisos en la salida de error y validar con LASTEXITCODE
$ErrorActionPreference = "Continue"

# validar que docker desktop este encendido
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker no responde. Abre Docker Desktop y vuelve a intentar." }

# convertir este equipo en manager si aun no pertenece a un swarm
$estado = docker info --format "{{.Swarm.LocalNodeState}}"
if ($estado -ne "active") {
    Write-Host "Iniciando Docker Swarm en este equipo..."
    docker swarm init
} else {
    Write-Host "Este equipo ya es parte de un Swarm."
}

# crear el secreto jwt_secret sin salto de linea al final
$existe = docker secret ls --filter name=jwt_secret --format "{{.Name}}"
if (-not $existe) {
    $clave = "siniestrofacil-clave-secreta-desarrollo-jvy0101-2026"
    $archivo = [System.IO.Path]::GetTempFileName()
    [System.IO.File]::WriteAllText($archivo, $clave)
    docker secret create jwt_secret $archivo
    Remove-Item $archivo
    Write-Host "Secreto jwt_secret creado."
} else {
    Write-Host "El secreto jwt_secret ya existe."
}

# mostrar los nodos del cluster
docker node ls
