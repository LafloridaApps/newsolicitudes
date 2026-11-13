# Entregas de intranet

Destino confirmado: desarrollo@192.168.10.49, /home/desarrollo/intranet. El alias desarrollo de /etc/hosts apunta a otra máquina; no usarlo para intranet.

Una entrega lleva una versión común intranet-AAAA.MM.DD.N y un tag Git por repositorio. Las imágenes incluyen las etiquetas OCI de versión y commit. No se hace push automáticamente. Se respetan las ramas de trabajo elegidas para la entrega.

El manifiesto se guarda en releases/<version>/manifest.json. Se construyen y transfieren las cinco imágenes con SHA-256 antes de reemplazar servicios. El runtime de cada entrega queda congelado en esa carpeta.

`python3 release_runtime.py prepare` conserva imágenes anteriores en rollback-images.tar y referencias rollback-<version>, configuraciones de contenedores, deploy.sh y .env, y dumps de solicitudes, funcionarios y departamentos. Los archivos quedan privados. Nunca imprimir los inspect completos, .env o dumps, porque contienen secretos/datos personales.

`python3 release_runtime.py deploy` conserva puertos, redes, volúmenes, límites y configuración Docker de los contenedores originales. Mantiene los contenedores previos detenidos con sufijo backup-<version>. Usa los .env del servidor. Para la nueva configuración de Cero Papel solo genera CEROPAPEL_ENCRYPTION_KEY si no existe; no sustituye una clave ya configurada.

Cada deploy.sh remoto se convierte en un wrapper del runtime de la entrega. Acepta deploy (predeterminado) o rollback. La activación valida salud por servicio y revierte todos los servicios de la entrega ante un fallo.

Rollback completo: `python3 /home/desarrollo/intranet/releases/<version>/release_runtime.py rollback`. Restaura contenedores, .env y deploy.sh anteriores. Los dumps son para recuperación manual: no se restaura automáticamente una base de datos porque podría eliminar escrituras concurrentes. Las tablas nuevas son aditivas y no se eliminan al revertir los contenedores.

Se verifica después la salud de APIs, el listado con campos adicionales, diagnóstico departamental, frontend a través de Nginx, rutas React y recursos. Nginx no se modifica. No ejecutar limpiezas globales de Docker.
