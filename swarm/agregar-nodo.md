# Agregar nodos al Swarm

Esta guía es para cuando el clúster tenga más de un equipo. La demo del proyecto usa un solo nodo, que cumple los roles de manager y worker.

## Requisitos de red

Todos los equipos deben verse en la misma red y tener abiertos estos puertos:

| Puerto | Protocolo | Uso |
| --- | --- | --- |
| 2377 | TCP | Administración del clúster, solo hacia los managers |
| 7946 | TCP y UDP | Comunicación entre nodos |
| 4789 | UDP | Tráfico de la red overlay |

## Agregar un worker

En el manager, obtén el comando con el token:

```powershell
docker swarm join-token worker
```

Copia la línea `docker swarm join --token ... <IP-del-manager>:2377` y ejecútala en el equipo nuevo. Luego, en el manager:

```powershell
docker node ls
```

## Agregar otro manager

```powershell
docker swarm join-token manager
```

Para tolerar la caída de un manager se necesitan 3 managers, porque Raft necesita mayoría: con 3 managers sigue funcionando si cae 1. Con 2 managers no se gana tolerancia.

## Imágenes en varios nodos

Las imágenes de este proyecto se construyen en local y no están en un registro. Con un solo nodo funciona, pero un worker no podría descargarlas. Con varios nodos hay que publicarlas en un registro, por ejemplo uno local en el manager:

```powershell
docker service create --name registro --publish 5000:5000 registry:2
docker tag ms-denuncias 127.0.0.1:5000/ms-denuncias
docker push 127.0.0.1:5000/ms-denuncias
```

Después, en `docker-stack.yml`, cambia cada `image:` a `127.0.0.1:5000/<nombre>`.

## Retirar un nodo

```powershell
# en el manager, mover sus replicas a otros nodos
docker node update --availability drain <nombre-del-nodo>
# en el nodo que sale
docker swarm leave
```
