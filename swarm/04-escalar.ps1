# cambiar el numero de replicas de un microservicio del stack
# ejemplo
#   powershell -ExecutionPolicy Bypass -File swarm\04-escalar.ps1 -Servicio ms-denuncias -Replicas 4

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("ms-usuarios", "ms-denuncias", "ms-polizas", "ms-talleres")]
    [string]$Servicio,

    [Parameter(Mandatory = $true)]
    [ValidateRange(0, 10)]
    [int]$Replicas
)

# escalar el servicio y esperar a que las replicas converjan
docker service scale "siniestrofacil_$Servicio=$Replicas"

# mostrar donde corre cada replica
docker service ps "siniestrofacil_$Servicio" --filter "desired-state=running"
