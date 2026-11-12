# Subrogancias en CeroPapel: primera integración demo

## Configuración

El backend usa por defecto `http://laflorida.demo.ceropapel.cl/inter/` y el usuario `app_test`.
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

- Al ingresar una solicitud se conserva la subrogancia local y se vincula con su solicitud. No se llama a CeroPapel.
- Al aprobar y firmar correctamente, después de confirmar la transacción local, se autentica, consulta los perfiles del jefe y llama a `activarSubrogancia.php`.
- Para solicitudes anteriores sin vínculo, solo se recupera un registro sin solicitud asociada que coincida exactamente con jefe, departamento, inicio y término. Si no existe coincidencia única, no se infiere otro período o cargo.
- El mantenedor pregunta si se quiere guardar también en CeroPapel. “Solo en este sistema” no llama a la API. “Volver” no guarda. La pregunta no se aplica a ediciones.
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
