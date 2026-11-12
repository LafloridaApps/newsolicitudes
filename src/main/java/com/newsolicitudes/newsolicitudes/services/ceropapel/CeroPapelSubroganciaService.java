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
    private static final Logger logger = LoggerFactory.getLogger(CeroPapelSubroganciaService.class);
    private final WebClient client;
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
            ApplicationEventPublisher events) {
        this.relaciones = relaciones;
        this.departamentos = departamentos;
        this.events = events;
        this.properties = properties;
        this.repository = repository;
        this.client = builder.baseUrl(properties.getBaseUrl()).build();
    }

    // Solo después del commit: un fallo de firma o de aprobación no debe activar cargos externos.
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviar(SubroganciaCeroPapelEvent event) {
        Subrogancia subrogancia = repository.findForUpdate(event.subroganciaId()).orElse(null);
        if (subrogancia == null || (subrogancia.getCeroPapelEstado() != null && !"PENDIENTE_ENVIO".equals(subrogancia.getCeroPapelEstado()))) {
            return;
        }
        try {
            // No convertir a inmediata: esa modalidad pierde el término automático del período.
            if (!subrogancia.getFechaInicio().isAfter(FechaUtils.fechaActual())) {
                throw new IllegalStateException("El inicio debe ser futuro para programar con término automático");
            }
            String token = autenticar();
            Map<String, Object> perfiles = obtenerPerfiles(token, subrogancia.getJefeDepartamento());
            Long perfilId = resolverPerfil(subrogancia, perfiles.get("perfiles"));
            validarSubrogante(token, subrogancia.getSubrogante());
            subrogancia.setCeroPapelPerfilId(perfilId);
            Map<String, Object> resultado = llamar("activarSubrogancia.php", Map.of(
                    "token", token, "perf_id", perfilId, "rut", subrogancia.getSubrogante(),
                    "fecha_inicio", subrogancia.getFechaInicio() + " 00:00:00",
                    "fecha_fin", subrogancia.getFechaFin() + " 23:59:59"));
            // No registrar cuerpos de autenticación ni tokens.
            logger.info("CeroPapel activarSubrogancia.php subroganciaLocal={} estado={} modo={} perf_id={} tare_id={} subr_id={} fecha_inicio={} fecha_fin={} msg={}",
                    subrogancia.getId(), resultado.get("estado"), resultado.get("modo"), resultado.get("perf_id"),
                    resultado.get("tare_id"), resultado.get("subr_id"), resultado.get("fecha_inicio"),
                    resultado.get("fecha_fin"), resultado.get("msg"));
            if (!"OK".equals(resultado.get("estado"))) {
                String motivo = texto(resultado.get("msg"));
                throw new IllegalStateException("CeroPapel rechazó la subrogancia: "
                        + (motivo.isBlank() ? "no informó el motivo" : motivo));
            }
            if (!"programada".equals(resultado.get("modo")) || resultado.get("tare_id") == null) {
                throw new IllegalStateException("CeroPapel no confirmó una programación con tare_id");
            }
            subrogancia.setCeroPapelTareaId(numero(resultado.get("tare_id")));
            subrogancia.setCeroPapelSubroganciaId(numero(resultado.get("subr_id")));
            subrogancia.setCeroPapelEstado("ENVIADA");
            subrogancia.setCeroPapelDetalle("Subrogancia programada correctamente en CeroPapel");
        } catch (Exception e) {
            subrogancia.setCeroPapelEstado(e instanceof PerfilPendienteException ? "PENDIENTE_PERFIL"
                    : e instanceof SubroganteSinPerfilException ? "SIN_PERFIL_SUBROGANTE" : "ERROR");
            String detalle = e instanceof IllegalStateException ? e.getMessage()
                    : "No se pudo confirmar el envío a CeroPapel. Revise la conexión antes de repetir la operación";
            subrogancia.setCeroPapelDetalle(detalle == null ? "Error de integración" : detalle.substring(0, Math.min(1900, detalle.length())));
            // Los errores de transporte pueden contener datos de la petición: no imprimirlos completos.
            if (e instanceof PerfilPendienteException) {
                logger.info("CeroPapel subroganciaLocal={} pendiente de selección de cargo en el mantenedor", subrogancia.getId());
            } else {
                logger.warn("CeroPapel subroganciaLocal={} no confirmada: {}", subrogancia.getId(),
                        e instanceof IllegalStateException ? e.getMessage() : e.getClass().getSimpleName());
            }
        }
        repository.save(subrogancia);
    }

    private void validarSubrogante(String token, Integer rut) {
        // getPerfilesRUT no entrega el identificador de institución: no confundir unidad con institución.
        // La pertenencia a la institución del cargo se valida finalmente en activarSubrogancia.
        Map<String, Object> respuesta = obtenerPerfiles(token, rut);
        if (perfiles(respuesta.get("perfiles")).isEmpty()) throw new SubroganteSinPerfilException();
    }

    private String autenticar() {
        if (properties.getPassword().isBlank()) throw new IllegalStateException("Configure CEROPAPEL_PASSWORD en el servidor");
        Map<String, Object> auth = llamar("autenticacion.php", Map.of(
                "usuario", properties.getUsuario(), "pass", properties.getPassword()));
        logger.info("CeroPapel autenticacion.php estado={}", auth.get("estado"));
        verificar(auth);
        Object token = auth.get("token");
        if (token == null || token.toString().isBlank()) throw new IllegalStateException("CeroPapel no entregó token");
        return token.toString();
    }

    private Map<String, Object> obtenerPerfiles(String token, Integer rut) {
        Map<String, Object> respuesta = llamar("getPerfilesRUT.php", Map.of("token", token, "rut", rut));
        logger.info("CeroPapel getPerfilesRUT.php rut={} estado={} perfiles={} msg={}",
                rut, respuesta.get("estado"), respuesta.get("perfiles"), respuesta.get("msg"));
        verificar(respuesta);
        return respuesta;
    }

    private List<ComparadorPerfilCeroPapel.Perfil> perfiles(Object datos) {
        if (!(datos instanceof List<?> lista)) return List.of();
        return lista.stream().filter(Map.class::isInstance).map(Map.class::cast)
                .map(p -> new ComparadorPerfilCeroPapel.Perfil(
                        numero(p.containsKey("codigo") ? p.get("codigo") : p.get("perf_id")),
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
                .filter(r -> properties.getBaseUrl().equals(r.getAmbiente()))
                .map(PerfilDepartamentoCeroPapel::getPerfilId)
                .orElse(properties.getPerfilesPorDepartamento().get(subrogancia.getIdDepto()));
        Long elegido;
        if (guardado != null) {
            // Una relación obsoleta debe confirmarse nuevamente, sin reemplazarla silenciosamente.
            elegido = perfiles.stream().anyMatch(p -> guardado.equals(p.id())) ? guardado : null;
        } else {
            elegido = ComparadorPerfilCeroPapel.comparar(nombre, perfiles);
        }
        return new ComparadorPerfilCeroPapel.Opciones(nombre, elegido, perfiles);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Subrogancia consultarRegistro(Long id) { return buscar(id); }

    @Transactional(readOnly = true)
    public ComparadorPerfilCeroPapel.Opciones consultarOpciones(Long id) {
        var s = buscar(id);
        return opciones(s, perfiles(obtenerPerfiles(autenticar(), s.getJefeDepartamento()).get("perfiles")));
    }

    @Transactional
    public void confirmarPerfil(Long id, Long perfilId) {
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
        var activos = perfiles(obtenerPerfiles(autenticar(), s.getJefeDepartamento()).get("perfiles"));
        if (perfilId == null || activos.stream().noneMatch(p -> perfilId.equals(p.id()))) {
            throw new SubroganciaException("Seleccione un cargo activo del jefe");
        }
        relaciones.save(new PerfilDepartamentoCeroPapel(
                s.getIdDepto(), perfilId, properties.getBaseUrl()));
        s.setCeroPapelEstado("PENDIENTE_ENVIO");
        repository.save(s);
        events.publishEvent(new SubroganciaCeroPapelEvent(s.getId()));
    }

    private Subrogancia buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new SubroganciaException("No existe la subrogancia"));
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private Map<String, Object> llamar(String endpoint, Map<String, Object> body) {
        try {
            // El demo devuelve JSON con Content-Type text/html: decodificar explícitamente.
            String respuesta = client.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(30));
            return leerRespuesta(endpoint, respuesta);
        } catch (WebClientResponseException e) {
            // Conservar endpoint y código HTTP, sin imprimir cuerpos con tokens o credenciales.
            throw new IllegalStateException("CeroPapel " + endpoint + " respondió HTTP " + e.getStatusCode().value());
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
        if (!"OK".equals(respuesta.get("estado"))) {
            // El detalle de activación ya se registra arriba; no exponer datos de autenticación.
            throw new IllegalStateException("CeroPapel respondió ERROR");
        }
    }

    private Long numero(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }
}
