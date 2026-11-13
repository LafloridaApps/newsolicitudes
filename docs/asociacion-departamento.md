# Asociación del departamento de origen

## Decisión

Se permite ingresar y consultar información, pero se exige una asociación departamental validada para crear solicitudes, incluidas las ingresadas por terceros. No se deduce la asociación por nombre. No se cambia la selección actual de contratos.

## Contrato

`GET /api/funcionario?rut=...` de api-funcionarios y su equivalente en newsolicitudes incorporan `configuracionDepartamento`:

```json
{"estado":"SIN_ASOCIACION","codigoExterno":"17100801","nombreExterno":"Of.Local de la Niñez","idDeptoInterno":null,"idDeptoAsociado":null}
```

El diagnóstico conserva el ID interno del funcionario antes de ocultarlo en la respuesta operativa y el ID obtenido de la asociación, si se pudo consultar. La interfaz muestra ambos valores, el código/nombre de SMC y los nulos explícitamente. Distingue `SIN_ID_INTERNO`, `ASOCIACION_INVALIDA` y `ASOCIACION_INCONSISTENTE`. Si el backend no entrega el diagnóstico, se informa `SIN_DIAGNOSTICO` en la interfaz; no se supone que la asociación falte.

Estados: `VALIDADO`, `SIN_ASOCIACION` (la consulta por código devolvió vacío/404), `SIN_CODIGO_ORIGEN`, `SERVICIO_NO_DISPONIBLE` y `NO_VERIFICADO` (respaldo local u otra inconsistencia). Solo VALIDADO permite crear solicitudes. La respuesta oculta departamento y jefatura anteriores cuando la asociación actual no se valida; la base local conserva sus datos para no destruir el historial.

## Corrección

En Departamentos se ofrece un formulario que recibe el código y nombre de origen por parámetros de navegación y permite seleccionar explícitamente el departamento interno. No realiza asignaciones automáticas. Se reutiliza `PUT /solicitudes/departamentos/{id}/codex?codEx=...`; el borrado existente también exige el mismo permiso.

El backend verifica la cookie `token` contra `/api/auth/me-funcionarios` y consulta los módulos del usuario autenticado: ADMINISTRACION o PARAMETROS. No toma un rutGestor del navegador como autorización. Las escrituras requieren además `X-Departamento-Admin: 1`. El botón solo se ofrece si `GET /solicitudes/departamentos/permiso-asociacion` autoriza la sesión.

Configurar AUTH_URL en newsolicitudes con la URL base del servicio de autenticación que sirve esa ruta. Por defecto se usa https://intranet.laflorida.cl. El servicio debe ser accesible desde el backend y responder `{ "success": true, "data": { "rut": "..." } }`. Se reenvía únicamente la cookie token al destino configurado. api-departamentos continúa siendo el servicio interno: su acceso directo debe estar restringido al backend; la autorización de la interfaz se aplica en newsolicitudes.

Se impide asociar otro código a un departamento que ya tenga uno, de acuerdo con el lector existente que espera una sola asociación por departamento. Cambiar una asociación existente requiere revisarla y eliminarla explícitamente. No se introducen tablas ni migraciones.

## Recuperación y despliegue

Guardar la asociación refresca el árbol y solicita revalidar al funcionario que está usando la sesión. Otro funcionario afectado puede usar Reintentar validación sin cerrar sesión. No se actualizan masivamente funcionarios ni se alteran solicitudes históricas.

Desplegar api-departamentos y api-funcionarios antes de newsolicitudes y la interfaz. Un backend anterior que no informe el estado no habilita nuevas solicitudes en la interfaz actual. No se modifican datos reales como parte de esta implementación.

## ADR

Se usa un estado explícito, separado de codDepto, porque un valor conservado en la base no demuestra una asociación vigente. Esto permite distinguir configuración incompleta y fallos transitorios, a costa de una comprobación adicional por código al construir la respuesta. La autorización usa una sesión validada en un servicio confiable y falla de forma cerrada cuando no puede verificarse.
