package com.newsolicitudes.newsolicitudes.services.ceropapel;

import com.newsolicitudes.newsolicitudes.repositories.PerfilDepartamentoCeroPapelRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import org.springframework.context.ApplicationEventPublisher;
import com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel;
import com.newsolicitudes.newsolicitudes.exceptions.SubroganciaException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.reactive.function.client.WebClient;
import com.newsolicitudes.newsolicitudes.config.CeroPapelProperties;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

@Service
public class CeroPapelSubroganciaService {
    private static final String MDC_REGISTRO = "ceroPapelRegistro";
    private static final String MDC_ORIGEN = "ceroPapelOrigen";
    private static final String TAREA_ID = "tare_id";
    private static final String ESTADO = "estado";
    private static final String PERFILES = "perfiles";
    private static final String TOKEN = "token";
    private static final String PERFIL_ID = "perf_id";
    private static final Logger logger = LoggerFactory.getLogger(CeroPapelSubroganciaService.class);
    private final WebClient.Builder clientBuilder;
    private final CeroPapelConfiguracion configuracion;
    private final ThreadLocal<CeroPapelConfiguracion.Conexion> conexionActual = new ThreadLocal<>();

    private static class IntegracionDesactivadaException extends org.springframework.web.server.ResponseStatusException {
        IntegracionDesactivadaException() {
            super(org.springframework.http.HttpStatus.CONFLICT,
                    "Integración Cero Papel desactivada. Registro guardado solo localmente.");
        }
    }
    private final CeroPapelProperties properties;
    private final SubroganciaRepository repository;
    private final PerfilDepartamentoCeroPapelRepository relaciones;
    private final DepartamentoService departamentos;
    private final ApplicationEventPublisher events;
    private static class SubroganteSinPerfilException extends IllegalStateException {
        SubroganteSinPerfilException() {
            super("El subrogante no tiene un perfil activo disponible en CeroPapel. Solicite su habilitación en demo o seleccione otro funcionario de prueba");
        }
    }
    private static class PerfilPendienteException extends IllegalStateException {
        PerfilPendienteException() { super("Seleccione y confirme el cargo en el mantenedor de subrogancias"); }
    }


    public CeroPapelSubroganciaService(WebClient.Builder builder, CeroPapelProperties properties,
            SubroganciaRepository repository,
            PerfilDepartamentoCeroPapelRepository relaciones,
            DepartamentoService departamentos,
            ApplicationEventPublisher events, CeroPapelConfiguracion configuracion) {
        this.relaciones = relaciones;
        this.departamentos = departamentos;
        this.events = events;
        this.properties = properties;
        this.repository = repository;
        this.clientBuilder = builder;
        this.configuracion = configuracion;
    }

    // Enviar únicamente después del commit que guarda la subrogancia local.
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviar(SubroganciaCeroPapelEvent event) {
        try (var origen = org.slf4j.MDC.putCloseable(MDC_ORIGEN, event.origen().name());
                var registro = org.slf4j.MDC.putCloseable(MDC_REGISTRO, String.valueOf(event.subroganciaId()))) {
            procesarEnvio(event);
        }
    }

    private void procesarEnvio(SubroganciaCeroPapelEvent event) {
        logger.info("CeroPapel envio inicio subroganciaLocal={} origen={}", event.subroganciaId(), event.origen());
        try {
            Subrogancia subrogancia = repository.findForUpdate(event.subroganciaId()).orElse(null);
            if (subrogancia == null || (subrogancia.getCeroPapelEstado() != null
                    && !"PENDIENTE_ENVIO".equals(subrogancia.getCeroPapelEstado()))) {
                logger.info("CeroPapel envio omitido subroganciaLocal={} origen={} motivo=ausente_o_procesada",
                        event.subroganciaId(), event.origen());
                return;
            }
            ejecutarEnvio(subrogancia);
            repository.save(subrogancia);
            logger.info("CeroPapel envio fin subroganciaLocal={} origen={} estado={}",
                    event.subroganciaId(), event.origen(), subrogancia.getCeroPapelEstado());
        } catch (RuntimeException e) {
            // El alta local ya fue confirmada: el fallo posterior se informa mediante el log.
            logger.error("CeroPapel envio fallo subroganciaLocal={} origen={} tipo={}",
                    event.subroganciaId(), event.origen(), e.getClass().getSimpleName());
        }
    }

    private void ejecutarEnvio(Subrogancia subrogancia) {
        try {
            conexionActual.set(configuracion.obtenerConexion());
            exigirHabilitada();
            programarSubrogancia(subrogancia);
        } catch (Exception e) {
            registrarError(subrogancia, e);
        } finally {
            conexionActual.remove();
        }
    }

    private void programarSubrogancia(Subrogancia subrogancia) {
        // No convertir a inmediata: esa modalidad pierde el término automático del período.
        if (!subrogancia.getFechaInicio().isAfter(FechaUtils.fechaActual())) {
            throw new IllegalStateException("El inicio debe ser futuro para programar con término automático");
        }
        String token = autenticar();
        Map<String, Object> perfiles = obtenerPerfiles(token, subrogancia.getJefeDepartamento());
        Long perfilId = resolverPerfil(subrogancia, perfiles.get(PERFILES));
        validarSubrogante(token, subrogancia.getSubrogante());
        subrogancia.setCeroPapelPerfilId(perfilId);
        Map<String, Object> resultado = llamar("activarSubrogancia.php", Map.of(
                TOKEN, token, PERFIL_ID, perfilId, "rut", subrogancia.getSubrogante(),
                "fecha_inicio", subrogancia.getFechaInicio() + " 00:00:00",
                "fecha_fin", subrogancia.getFechaFin() + " 23:59:59"));
        // No registrar cuerpos de autenticación ni tokens.
        logger.info("CeroPapel activarSubrogancia.php subroganciaLocal={} estado={} modo={} perf_id={} tare_id={} subr_id={} fecha_inicio={} fecha_fin={} msg={}",
                subrogancia.getId(), resultado.get(ESTADO), resultado.get("modo"), resultado.get(PERFIL_ID),
                resultado.get(TAREA_ID), resultado.get("subr_id"), resultado.get("fecha_inicio"),
                resultado.get("fecha_fin"), resultado.get("msg"));
        validarProgramacion(resultado);
        subrogancia.setCeroPapelTareaId(numero(resultado.get(TAREA_ID)));
        subrogancia.setCeroPapelSubroganciaId(numero(resultado.get("subr_id")));
        subrogancia.setCeroPapelEstado("ENVIADA");
        subrogancia.setCeroPapelDetalle("Subrogancia programada correctamente en CeroPapel");
    }

    private void validarProgramacion(Map<String, Object> resultado) {
        if (!"OK".equals(resultado.get(ESTADO))) {
            String motivo = texto(resultado.get("msg"));
            throw new IllegalStateException("CeroPapel rechazó la subrogancia: "
                    + (motivo.isBlank() ? "no informó el motivo" : motivo));
        }
        if (!"programada".equals(resultado.get("modo")) || resultado.get(TAREA_ID) == null) {
            throw new IllegalStateException("CeroPapel no confirmó una programación con tare_id");
        }
    }

    private void registrarError(Subrogancia subrogancia, Exception e) {
        String estadoError = "ERROR";
        if (e instanceof IntegracionDesactivadaException) {
            estadoError = "DESACTIVADA";
        } else if (e instanceof PerfilPendienteException) {
            estadoError = "PENDIENTE_PERFIL";
        } else if (e instanceof SubroganteSinPerfilException) {
            estadoError = "SIN_PERFIL_SUBROGANTE";
        }
        subrogancia.setCeroPapelEstado(estadoError);
        String detalle = detalleError(e);
        subrogancia.setCeroPapelDetalle(detalle == null ? "Error de integración" : detalle.substring(0, Math.min(1900, detalle.length())));
        // Los errores de transporte pueden contener datos de la petición: no imprimirlos completos.
        if (e instanceof IntegracionDesactivadaException) {
            logger.info("CeroPapel subroganciaLocal={} integración desactivada: solo registro local", subrogancia.getId());
        } else if (e instanceof PerfilPendienteException) {
            logger.info("CeroPapel subroganciaLocal={} pendiente de selección de cargo en el mantenedor", subrogancia.getId());
        } else {
            logger.warn("CeroPapel subroganciaLocal={} no confirmada: {}", subrogancia.getId(),
                    e instanceof IllegalStateException ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    private String detalleError(Exception error) {
        if (error instanceof IntegracionDesactivadaException desactivada) return desactivada.getReason();
        if (error instanceof IllegalStateException) return error.getMessage();
        return "No se pudo confirmar el envío a CeroPapel. Revise la conexión antes de repetir la operación";
    }

    private void validarSubrogante(String token, Integer rut) {
        // getPerfilesRUT no entrega el identificador de institución: no confundir unidad con institución.
        // La pertenencia a la institución del cargo se valida finalmente en activarSubrogancia.
        Map<String, Object> respuesta = obtenerPerfiles(token, rut);
        if (perfiles(respuesta.get(PERFILES)).isEmpty()) throw new SubroganteSinPerfilException();
    }

    private String autenticar() {
        if (conexion().clave().isBlank()) throw new IllegalStateException("Configure CEROPAPEL_PASSWORD en el servidor");
        Map<String, Object> auth = llamar("autenticacion.php", Map.of(
                "usuario", conexion().usuario(), "pass", conexion().clave()));
        logger.info("CeroPapel autenticacion.php estado={}", auth.get(ESTADO));
        verificar(auth);
        Object token = auth.get(TOKEN);
        if (token == null || token.toString().isBlank()) throw new IllegalStateException("CeroPapel no entregó token");
        return token.toString();
    }

    private Map<String, Object> obtenerPerfiles(String token, Integer rut) {
        Map<String, Object> respuesta = llamar("getPerfilesRUT.php", Map.of(TOKEN, token, "rut", rut));
        logger.info("CeroPapel getPerfilesRUT.php estado={}", respuesta.get(ESTADO));
        verificar(respuesta);
        return respuesta;
    }

    private List<ComparadorPerfilCeroPapel.Perfil> perfiles(Object datos) {
        if (!(datos instanceof List<?> lista)) return List.of();
        return lista.stream().filter(Map.class::isInstance).map(Map.class::cast)
                .map(p -> new ComparadorPerfilCeroPapel.Perfil(
                        numero(p.get(p.containsKey("codigo") ? "codigo" : PERFIL_ID)),
                        texto(p.get("perf_nombre")), texto(p.get("uni_nombre"))))
                .filter(p -> p.id() != null).distinct().toList();
    }

    private String texto(Object value) { return value == null ? "" : value.toString(); }

    private Long resolverPerfil(Subrogancia subrogancia, Object datos) {
        var opciones = opciones(subrogancia, perfiles(datos));
        if (opciones.perfilSugerido() == null) throw new PerfilPendienteException();
        logger.info("CeroPapel departamento={} cargo seleccionado={}", subrogancia.getIdDepto(), opciones.perfilSugerido());
        return opciones.perfilSugerido();
    }

    private ComparadorPerfilCeroPapel.Opciones opciones(Subrogancia subrogancia,
            List<ComparadorPerfilCeroPapel.Perfil> perfiles) {
        String nombre = departamentos.getDepartamentoById(subrogancia.getIdDepto()).getNombre();
        Long guardado = relaciones.findById(subrogancia.getIdDepto())
                .filter(r -> conexion().url().equals(r.getAmbiente()))
                .map(PerfilDepartamentoCeroPapel::getPerfilId)
                .orElse(perfilConfigurado(subrogancia.getIdDepto()));
        Long elegido;
        if (guardado != null) {
            // Una relación obsoleta debe confirmarse nuevamente, sin reemplazarla silenciosamente.
            elegido = perfiles.stream().anyMatch(p -> guardado.equals(p.id())) ? guardado : null;
        } else {
            elegido = ComparadorPerfilCeroPapel.comparar(nombre, perfiles);
        }
        return new ComparadorPerfilCeroPapel.Opciones(nombre, elegido, perfiles);
    }

    private Long perfilConfigurado(Long departamentoId) {
        if (!conexion().url().equals(properties.getBaseUrl())) return null;
        return properties.getPerfilesPorDepartamento().get(departamentoId);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Subrogancia consultarRegistro(Long id) { return buscar(id); }

    @Transactional(readOnly = true)
    public ComparadorPerfilCeroPapel.Opciones consultarOpciones(Long id) {
        try (var origen = org.slf4j.MDC.putCloseable(MDC_ORIGEN, "CONSULTA_MANUAL");
                var registro = org.slf4j.MDC.putCloseable(MDC_REGISTRO, String.valueOf(id))) {
            conexionActual.set(configuracion.obtenerConexion());
            exigirHabilitada();
            var s = buscar(id);
            return opciones(s, perfiles(obtenerPerfiles(autenticar(), s.getJefeDepartamento()).get(PERFILES)));
        } finally {
            conexionActual.remove();
        }
    }

    @Transactional
    public void confirmarPerfil(Long id, Long perfilId) {
        try (var origen = org.slf4j.MDC.putCloseable(MDC_ORIGEN, "CONFIRMACION_MANUAL");
                var registro = org.slf4j.MDC.putCloseable(MDC_REGISTRO, String.valueOf(id))) {
            conexionActual.set(configuracion.obtenerConexion());
            exigirHabilitada();
            guardarPerfilConfirmado(id, perfilId);
        } finally {
            conexionActual.remove();
        }
    }

    private void guardarPerfilConfirmado(Long id, Long perfilId) {
        // Serializar la confirmación evita que dos clics programen el mismo registro dos veces.
        var s = repository.findForUpdate(id).orElseThrow(() ->
                new SubroganciaException("No existe la subrogancia"));
        if (!("PENDIENTE_PERFIL".equals(s.getCeroPapelEstado())
                || ("ERROR".equals(s.getCeroPapelEstado()) && s.getCeroPapelPerfilId() == null))) {
            throw new SubroganciaException(
                    "El registro no está pendiente de selección de cargo. Revise el resultado anterior antes de reenviar");
        }
        if (!s.getFechaInicio().isAfter(FechaUtils.fechaActual())) {
            throw new SubroganciaException("El inicio debe ser futuro");
        }
        var activos = perfiles(obtenerPerfiles(autenticar(), s.getJefeDepartamento()).get(PERFILES));
        if (perfilId == null || activos.stream().noneMatch(p -> perfilId.equals(p.id()))) {
            throw new SubroganciaException("Seleccione un cargo activo del jefe");
        }
        relaciones.save(new PerfilDepartamentoCeroPapel(
                s.getIdDepto(), perfilId, conexion().url()));
        s.setCeroPapelEstado("PENDIENTE_ENVIO");
        repository.save(s);
        events.publishEvent(new SubroganciaCeroPapelEvent(s.getId(), SubroganciaCeroPapelEvent.Origen.CONFIRMACION_MANUAL));
    }

    private Subrogancia buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new SubroganciaException("No existe la subrogancia"));
    }

    private CeroPapelConfiguracion.Conexion conexion() {
        var actual = conexionActual.get();
        if (actual == null) throw new IllegalStateException("No hay una operación de Cero Papel en curso");
        return actual;
    }

    private void exigirHabilitada() {
        if (!conexion().habilitada() || !configuracion.estaHabilitada()) {
            throw new IntegracionDesactivadaException();
        }
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private Map<String, Object> llamar(String endpoint, Map<String, Object> body) {
        exigirHabilitada();
        String llamadaId = java.util.UUID.randomUUID().toString();
        String origen = java.util.Objects.requireNonNullElse(
                org.slf4j.MDC.get(MDC_ORIGEN), "CONSULTA_MANUAL");
        String registro = java.util.Objects.requireNonNullElse(
                org.slf4j.MDC.get(MDC_REGISTRO), "NO_APLICA");
        logger.info("CeroPapel api inicio llamadaId={} endpoint={} origen={} subroganciaLocal={}",
                llamadaId, endpoint, origen, registro);
        String error = null;
        try {
            // El demo devuelve JSON con Content-Type text/html: decodificar explícitamente.
            String respuesta = clientBuilder.clone().baseUrl(conexion().url()).build().post().uri(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(30));
            Map<String, Object> datos = leerRespuesta(endpoint, respuesta);
            if (logger.isInfoEnabled()) {
                logger.info("CeroPapel api respuesta llamadaId={} endpoint={} origen={} subroganciaLocal={} estado={}",
                        llamadaId, endpoint, origen, registro, texto(datos.get(ESTADO)));
            }
            return datos;
        } catch (WebClientResponseException e) {
            error = "http=" + e.getStatusCode().value();
            throw new IllegalStateException("CeroPapel " + endpoint + " respondió HTTP " + e.getStatusCode().value(), e);
        } catch (RuntimeException e) {
            error = "tipo=" + e.getClass().getSimpleName();
            throw new IllegalStateException("CeroPapel " + endpoint + " no pudo completar la llamada", e);
        } finally {
            if (error != null) {
                logger.warn("CeroPapel api error llamadaId={} endpoint={} origen={} subroganciaLocal={} {}",
                        llamadaId, endpoint, origen, registro, error);
            }
        }
    }

    static Map<String, Object> leerRespuesta(String endpoint, String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            throw new IllegalStateException("CeroPapel " + endpoint + " devolvió una respuesta vacía");
        }
        try {
            Map<String, Object> datos = JSON.readValue(respuesta, new TypeReference<Map<String, Object>>() {});
            if (datos == null) throw new IllegalArgumentException();
            return datos;
        } catch (RuntimeException e) {
            throw new IllegalStateException("CeroPapel " + endpoint + " no devolvió un objeto JSON válido");
        }
    }

    private void verificar(Map<String, Object> respuesta) {
        if (!"OK".equals(respuesta.get(ESTADO))) {
            // El detalle de activación ya se registra arriba; no exponer datos de autenticación.
            throw new IllegalStateException("CeroPapel respondió ERROR");
        }
    }

    private Long numero(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }
}
