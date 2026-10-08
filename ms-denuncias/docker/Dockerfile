# imagen de MySQL para SiniestroFacil
FROM mysql:8.4

ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=siniestrofacil
ENV MYSQL_USER=sfuser
ENV MYSQL_PASSWORD=sfpass
ENV TZ=America/Santiago

# este script se ejecuta automaticamente la primera vez que arranca el contenedor
COPY ./create.sql /docker-entrypoint-initdb.d/
