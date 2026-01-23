#!/bin/bash

# =========================================================
# CONFIGURACIÓN DEL MICROSERVICIO
# =========================================================
NOMBRE_APP="new-solicitudes"                 # Nombre del contenedor
PUERTO="8081"                       # Puerto que usa la App
IMAGEN_HUB="mirkogutierrezappx/new-solicitudes" # Repositorio en Docker Hub
# =========================================================

OPCION=${1:-"dev"}

case $OPCION in
    "prod")
        echo "--- MODO PRODUCCIÓN: Bajando imagen de la nube ($IMAGEN_HUB) ---"
        docker pull $IMAGEN_HUB:latest
        TARGET_IMAGE="$IMAGEN_HUB:latest"
        ;;
    *)
        echo "--- MODO DESARROLLO: Compilando localmente ($NOMBRE_APP) ---"
        ./mvnw clean package -DskipTests
        docker build -t $NOMBRE_APP:local .
        TARGET_IMAGE="$NOMBRE_APP:local"
        ;;
esac

echo "--- Limpiando contenedor anterior ---"
docker stop ${NOMBRE_APP}-container 2>/dev/null
docker rm ${NOMBRE_APP}-container 2>/dev/null

echo "--- Iniciando contenedor en puerto $PUERTO ---"
docker run \
           --restart always \
           -d -p ${PUERTO}:${PUERTO} \
           --env-file .env \
           --network appx \
           --add-host=host.docker.internal:host-gateway \
           --name ${NOMBRE_APP}-container \
           $TARGET_IMAGE

docker image prune -f
echo "--- Proceso Terminado ($OPCION) ---"
