# Entrega intranet-2026.11.13.1

Servidor confirmado: 192.168.10.49 (Srv-Intranet). Fecha: 13 de noviembre de 2026.

| Componente | Rama | Commit | Imagen |
|---|---|---|---|
| smcapis | codex/contratos-y-asociacion-departamentos | 6b55beba | smcapis:intranet-2026.11.13.1 |
| api-departamentos | codex/contratos-y-asociacion-departamentos | db98e9b4 | api-departamentos:intranet-2026.11.13.1 |
| api-funcionarios | codex/contratos-y-asociacion-departamentos | e48eb8ce | api-funcionarios:intranet-2026.11.13.1 |
| newsolicitudes | codex/main-integracion-ceropapel | 2c375b60 | newsolicitudes:intranet-2026.11.13.1 |
| app-feriados | codex/main-integracion-ceropapel | 018ecc01 | image-app-feriados:intranet-2026.11.13.1 |

Tag Git común: intranet-2026.11.13.1. Sin push automático. Runtime de despliegue: ec48fc912aedac41524b55e3caf2a9b52e6a88d3 (tag intranet-2026.11.13.1-runtime.1).

## Respaldo y recuperación

Carpeta privada del servidor: /home/desarrollo/intranet/releases/intranet-2026.11.13.1. Contiene manifiesto, imágenes anteriores exportadas con SHA-256, dumps de solicitudes/funcionarios/departamentos, configuración anterior y verificación. Se conservaron los cinco contenedores previos detenidos con sufijo backup-intranet-2026.11.13.1.

Rollback completo desde el servidor:

```bash
python3 /home/desarrollo/intranet/releases/intranet-2026.11.13.1/release_runtime.py rollback
```

Los datos no se restauran automáticamente: las tablas añadidas son aditivas y se evita sobrescribir escrituras concurrentes. Los dumps quedan disponibles para recuperación manual si fuera necesaria.

## Configuración y validación

Puertos, redes, volúmenes y Nginx conservados. Los .env de las tres APIs no cambiaron. En new-solicitudes se configuró AUTH_URL con la ruta interna http://192.168.10.49:8082 porque el dominio público no es alcanzable desde el servidor. La clave maestra de Cero Papel se genera en el servidor únicamente cuando falta; se conserva con el entorno y el respaldo privado. Cero Papel quedó deshabilitado, sin invocaciones reales de envío.

Cuatro APIs con salud UP; cinco contenedores activos sin reinicios. Verificados 39 contratos, vigentes y no vigentes, con numresolcontr y obs_adicional_cont. El diagnóstico 17100801 se propaga hasta solicitudes y en esta base remota figura VALIDADO. Frontend, recarga de ruta React, bundle nuevo y 404 de assets inexistentes verificados a través de Nginx.

Las pruebas de backend, frontend y runtime pasaron. El redeploy de prueba en el servidor fue rechazado por la revisión automática por su posible impacto; se sustituyó por pruebas locales del runtime e inspecciones sin reiniciar ni reemplazar contenedores. No se verificó una sesión administrativa real ni se modificaron asociaciones reales.
