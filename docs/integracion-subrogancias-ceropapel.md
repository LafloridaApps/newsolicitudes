# Subrogancias en CeroPapel: primera integración demo

## Configuración

La URL y clave iniciales provienen de CEROPAPEL_BASE_URL y CEROPAPEL_PASSWORD en el entorno; el usuario inicial es app_test. El panel de Ingreso de subrogancias permite editar la configuración persistida y desactivar globalmente la integración.
Configurar `CEROPAPEL_PASSWORD` en el entorno del proceso Java o del contenedor con la contraseña de prueba proporcionada. La contraseña y el token no se registran en consola ni se incluyen en el código.
`CEROPAPEL_BASE_URL` y `CEROPAPEL_USUARIO` permiten cambiar estos valores.

Si el jefe tiene varios cargos activos, configurar explícitamente el perfil por departamento en las propiedades del servidor, por ejemplo:

```properties
ceropapel.perfiles-por-departamento[10]=20439297
```

El perfil configurado debe aparecer en los perfiles activos del jefe. Sin relación guardada ni configuración explícita, se compara el nombre del departamento local con `uni_nombre` de cada perfil. Se normalizan tildes, mayúsculas, signos, espacios y artículos/preposiciones. Si no viene unidad, se busca la frase completa del departamento en el cargo (al menos dos palabras significativas). Solo se elige una coincidencia única; un perfil único cuyo nombre no coincide también requiere confirmación.

Si no hay coincidencia única, el registro queda `PENDIENTE_PERFIL`. Al crear manualmente se abre un selector de cargos; también aparece “Elegir cargo” en los resultados del mantenedor, para altas manuales y solicitudes aprobadas. La confirmación valida que el cargo siga activo, guarda la relación departamento/perfil/ambiente en `perfil_departamento_cero_papel` y envía después del commit. Se reutiliza esa relación en siguientes envíos, verificando los perfiles activos del jefe. Si dejó de estar disponible, vuelve a pedir confirmación.

Los registros con `ERROR` y sin perfil externo seleccionado pueden resolverse desde esa misma acción, por ejemplo los que fallaron por contraseña ausente o múltiples cargos antes de esta mejora. No se reenvían registros ya enviados ni errores posteriores a seleccionar un perfil, porque su resultado externo podría ser incierto. La confirmación y el envío bloquean el registro para impedir envíos simultáneos.

## Flujo

- Al generar una solicitud que crea una subrogancia, se guarda primero en la base local. Después del commit de la solicitud, se autentica, consulta los perfiles y llama a `activarSubrogancia.php`. No se espera a la aprobación o firma.
- Si falla CeroPapel, se conservan la solicitud y la subrogancia local y se registra el motivo en el log y el estado de integración.
- Las altas del mantenedor se envían automáticamente después del commit local, sin preguntar si se desea enviar. La respuesta incluye `id`, `ceroPapelEstado` y `ceroPapelDetalle`; la pantalla muestra un aviso si el envío falla o no queda confirmado. La selección de un cargo ambiguo sigue requiriendo confirmación. El campo legado `grabarCeroPapel` ya no permite omitir el envío.
- Se envían fechas explícitas de 00:00:00 a 23:59:59, incluyendo el último día. Esta convención debe validarse con la zona horaria del proveedor.
- Se exige inicio posterior al día actual en Santiago. Para hoy o fechas anteriores se conserva el registro local y se registra ERROR. No se activa una subrogancia inmediata sin fecha de término.
- La consola registra el resultado de los endpoints y los identificadores externos; no registra contraseña ni token.
- Se conservan `ceroPapelEstado`, `ceroPapelPerfilId`, `ceroPapelTareaId` y `ceroPapelSubroganciaId` en la subrogancia. `ENVIADA` indica programación aceptada, no confirmación de que ya comenzó.
- Ante fallo externo se conserva la aprobación y se registra ERROR. No hay reintentos automáticos: una interrupción podría ocurrir después de que CeroPapel ejecutó la operación.

Las nuevas columnas se crean mediante la configuración existente `spring.jpa.hibernate.ddl-auto=update`.

## Alcance pendiente

Esta etapa implementa altas. Editar, borrar o anular localmente todavía no llama a `caducarSubrogancia.php` ni modifica programaciones externas. El mantenedor muestra el estado de envío, además del estado local del período; no consulta la vigencia remota.
No hay consulta remota de estado, recuperación automática tras caída entre commit y envío, ni reenvío automático de registros procesados. Un resultado incierto requiere revisión en demo antes de repetir una operación.

## Validación

Las pruebas simulan las respuestas HTTP sin acceder al demo y comprueban envío después del commit, ausencia de envío tras rollback, selección del perfil, tratamiento de errores y fechas no futuras. Antes de la prueba real, configurar la contraseña y utilizar funcionarios y perfiles habilitados en demo.

## Validación del subrogante y motivos de rechazo

Antes de activar se consulta `getPerfilesRUT` para el RUT del subrogante. Si no devuelve perfiles activos, se registra `SIN_PERFIL_SUBROGANTE` y no se intenta activar. Un perfil activo no garantiza que pertenezca a la institución del cargo: la respuesta de perfiles no entrega ese identificador y no se compara unidad como si fuera institución. La comprobación definitiva sigue a cargo de `activarSubrogancia`.

El motivo de rechazo o de la validación se guarda en `cero_papel_detalle` y se muestra al guardar y al consultar el mantenedor. La aprobación y el registro local se conservan si falla la operación externa. No se habilitan usuarios ni se crean perfiles en CeroPapel desde esta integración.

## Decisión: alta automática después del commit (2026-11-12)

Contexto: tanto las solicitudes como el mantenedor deben intentar registrar todas las nuevas subrogancias en CeroPapel sin perder el alta local ante fallos externos.

Decisión: el servicio compartido publica siempre el evento de envío tras guardar; el listener lo procesa después del commit. El mantenedor consulta el estado en una transacción nueva para devolver el resultado real. Se completan los endpoints de consulta y confirmación de cargo usados por la pantalla y los estados de integración en búsquedas.

Consecuencias: los fallos externos no revierten el alta. Las solicitudes no muestran alertas de integración; el mantenedor sí. Se mantienen la validación de fecha futura, la selección explícita de cargos ambiguos y la ausencia de reintentos automáticos. No cambia el esquema por esta modificación.

## Trazabilidad de llamadas a la API

Cada envío registra inicio y fin con `subroganciaLocal`, `origen` (AUTOMATICO_SOLICITUD, MANUAL o CONFIRMACION_MANUAL) y estado final. Los envíos omitidos también dejan constancia. Cada llamada HTTP registra inicio y respuesta o error, con `llamadaId`, endpoint, origen e identificador local. Los errores HTTP incluyen el código; los de transporte o respuesta inválida incluyen el tipo de excepción. No se registran cuerpos de peticiones, contraseñas ni tokens. Las consultas manuales de cargos incluyen también el identificador local.

Estos mensajes se emiten mediante el logger del backend; su destino y retención dependen de la configuración de logs del despliegue.

## Control global desde el mantenedor

El panel «Integración Cero Papel» muestra el estado y permite guardar URL, usuario y clave al administrador con módulo CONFIGURACION CERO PAPEL y sesión verificada. La clave no se devuelve al navegador. Desactivar conserva las altas locales con estado DESACTIVADA y bloquea nuevas llamadas externas tanto manuales como automáticas. Reactivar no reenvía registros anteriores ni cambia las programaciones que Cero Papel ya aceptó.

Ver docs/diseno-configuracion-ceropapel.md para instalación, variables, permisos y alcance de una llamada en curso.
