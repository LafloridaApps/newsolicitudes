#!/bin/bash

# =========================================================
# CONFIGURACIÓN DEL MICROSERVICIO Y SERVIDOR
# =========================================================
NOMBRE_APP="newsolicitudes"
PUERTO="8081"
TARGET_IMAGE="$NOMBRE_APP:v1.0.0"
NETWORK="laflorida"
# =========================================================

# 1. Construcción local
echo "Elimnando  imagen anterior"
docker rmi -f $TARGET_IMAGE 2>/dev/null 
echo "--- 1. Creando archivo JAR y construyendo imagen local ---"
./mvnw clean package -DskipTests && \
echo "Construyendo imagen local..." && \
docker build -t $TARGET_IMAGE .

if [ $? -ne 0 ]; then
    echo "❌ Error en la construcción local. Abortando."
    exit 1
fi




# 5. Limpieza local
echo "--- 5. Limpieza de imágenes locales ---"
docker image prune -f

echo "✅ ¡Proceso completado! La aplicación $NOMBRE_APP ya está corriendo en $REMOTO:$PUERTO"