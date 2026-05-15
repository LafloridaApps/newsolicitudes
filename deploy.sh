#!/bin/bash

# =========================================================
# CONFIGURACIÓN DEL MICROSERVICIO Y SERVIDOR
# =========================================================
NOMBRE_APP="newsolicitudes"
PUERTO="8081"
TARGET_IMAGE="$NOMBRE_APP:local"
REMOTO="desarrollo@app-server"
NETWORK="laflorida"
# =========================================================

# 1. Construcción local
echo "--- 1. Creando archivo JAR y construyendo imagen local ---"
./mvnw clean package -DskipTests && \
echo "Elimnando imagen local anterior (si existe)..." && \
docker rmi -f $TARGET_IMAGE 2>/dev/null || true && \
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