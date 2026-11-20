#!/bin/bash
# iniciar el jar de spring boot dentro de la lambda
# el lambda web adapter traduce los eventos de api gateway a peticiones http al puerto 8081
# reducir la compilacion jit para acelerar el arranque en frio
exec java -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -jar /var/task/app.jar
