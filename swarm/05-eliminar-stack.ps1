# eliminar el stack y opcionalmente salir del swarm
# los datos de mysql quedan guardados en el volumen siniestrofacil_datos-mysql

param([switch]$SalirDelSwarm)

docker stack rm siniestrofacil

if ($SalirDelSwarm) {
    # esperar a que se borren los servicios antes de salir
    Start-Sleep -Seconds 15
    docker secret rm jwt_secret
    docker swarm leave --force
}
