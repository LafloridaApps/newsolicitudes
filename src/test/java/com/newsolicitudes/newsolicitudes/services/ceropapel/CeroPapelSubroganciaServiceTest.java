package com.newsolicitudes.newsolicitudes.services.ceropapel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.newsolicitudes.newsolicitudes.config.CeroPapelProperties;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.SubroganciaException;
import com.newsolicitudes.newsolicitudes.repositories.PerfilDepartamentoCeroPapelRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaServiceImpl;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class CeroPapelSubroganciaServiceTest {
    private final Estado estado = new Estado();
    private Subrogancia subrogancia;

    static class Estado {
        private final List<String> llamadas = new ArrayList<>();
        private final List<String> destinos = new ArrayList<>();
        private final List<String> cuerpos = new ArrayList<>();
        private final Map<Long, PerfilDepartamentoCeroPapel> relaciones = new HashMap<>();
        private String perfiles;
        private String perfilesSubrogante;
        private String activacion;
        private String tipoRespuesta;
        private HttpStatus httpStatus;
        private Subrogancia registro;
        private int guardados;
        private boolean desactivarTrasAutenticacion;
    }

    @TestConfiguration
    @EnableTransactionManagement
    @Import({CeroPapelSubroganciaService.class, SubroganciaServiceImpl.class})
    static class Config {
        @Bean CeroPapelConfiguracion configuracion(CeroPapelProperties properties) {
            return new CeroPapelConfiguracion() {
                public Conexion obtenerConexion() {
                    return new Conexion(properties.isHabilitada(), properties.getBaseUrl(),
                            properties.getUsuario(), properties.getPassword());
                }
                public boolean estaHabilitada() { return properties.isHabilitada(); }
            };
        }

        @Bean CeroPapelProperties properties() {
            CeroPapelProperties p = new CeroPapelProperties();
            p.setPassword("test-password");
            p.setBaseUrl("http://laflorida.demo.ceropapel.cl/inter/");
            return p;
        }
        @Bean SubroganciaRepository repository(Estado estado) {
            return (SubroganciaRepository) Proxy.newProxyInstance(
                SubroganciaRepository.class.getClassLoader(), new Class<?>[] {SubroganciaRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById", "findForUpdate" -> Optional.ofNullable(estado.registro);
                    case "save" -> {
                        estado.guardados++;
                        estado.registro = (Subrogancia) args[0];
                        if (estado.registro.getId() == null) estado.registro.setId(1L);
                        yield estado.registro;
                    }
                    case "existeSubrogancia" -> Optional.empty();
                    case "toString" -> "TestSubroganciaRepository";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        }
        @Bean PerfilDepartamentoCeroPapelRepository relacionesRepository(Estado estado) {
            return (PerfilDepartamentoCeroPapelRepository)
                Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {PerfilDepartamentoCeroPapelRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findById" -> Optional.ofNullable(estado.relaciones.get(args[0]));
                        case "save" -> {
                            var r = (PerfilDepartamentoCeroPapel) args[0];
                            estado.relaciones.put(r.getIdDepto(), r);
                            yield r;
                        }
                        case "toString" -> "RelacionesTest";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
        @Bean DepartamentoService departamentos() {
            return (DepartamentoService)
                Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {DepartamentoService.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getDepartamentoById")) {
                            var d = new DepartamentoResponse();
                            d.setNombre("Tecnologías de la Información");
                            return d;
                        }
                        if (method.getName().equals("toString")) return "DepartamentosTest";
                        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                        if (method.getName().equals("equals")) return proxy == args[0];
                        throw new UnsupportedOperationException(method.getName());
                    });
        }
        @Bean PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(new DriverManagerDataSource("jdbc:h2:mem:ceropapel"));
        }
        @Bean WebClient.Builder builder(Estado estado, CeroPapelProperties properties) {
            return WebClient.builder().exchangeFunction(request -> {
                String endpoint = request.url().getPath();
                estado.llamadas.add(endpoint);
                estado.destinos.add(request.url().getHost());
                String body = respuestaParaEndpoint(endpoint, estado);
                var salida = new MockClientHttpRequest(request.method(), request.url());
                return request.writeTo(salida, ExchangeStrategies.withDefaults())
                    .then(Mono.defer(salida::getBodyAsString)).map(json -> {
                        estado.cuerpos.add(json);
                        String respuesta = endpoint.endsWith("getPerfilesRUT.php") && json.contains("\"rut\":222")
                            ? estado.perfilesSubrogante : body;
                        if (estado.desactivarTrasAutenticacion && endpoint.endsWith("autenticacion.php")) {
                            properties.setHabilitada(false);
                        }
                        return ClientResponse.create(estado.httpStatus)
                            .header("Content-Type", estado.tipoRespuesta).body(respuesta).build();
                    });
            });
        }
        private String respuestaParaEndpoint(String endpoint, Estado estado) {
            if (endpoint.endsWith("autenticacion.php")) {
                return "{\"estado\":\"OK\",\"token\":\"test-token\"}";
            }
            return endpoint.endsWith("getPerfilesRUT.php") ? estado.perfiles : estado.activacion;
        }

    }

    @BeforeEach void preparar() {
        estado.httpStatus = HttpStatus.OK;
        estado.llamadas.clear();
        estado.cuerpos.clear();
        estado.guardados = 0;
        estado.tipoRespuesta = MediaType.APPLICATION_JSON_VALUE;
        estado.perfilesSubrogante = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":999}]}";
        estado.relaciones.clear();
        estado.perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":\"123\",\"uni_nombre\":\"TECNOLOGIAS DE LA INFORMACION\"}]}";
        estado.activacion = "{\"estado\":\"OK\",\"modo\":\"programada\",\"tare_id\":456}";
        subrogancia = new Subrogancia(222, FechaUtils.fechaActual().plusDays(2),
                FechaUtils.fechaActual().plusDays(3), 111, 10L);
        subrogancia.setId(1L);
        estado.registro = subrogancia;
    }

    private AnnotationConfigApplicationContext crearContexto() {
        var contexto = new AnnotationConfigApplicationContext();
        contexto.registerBean(Estado.class, () -> estado);
        contexto.register(Config.class);
        contexto.refresh();
        return contexto;
    }

    private void publicar(AnnotationConfigApplicationContext ctx, boolean rollback) {
        int llamadasPrevias = estado.llamadas.size();
        new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)).executeWithoutResult(status -> {
            ctx.publishEvent(new SubroganciaCeroPapelEvent(1L));
            assertEquals(llamadasPrevias, estado.llamadas.size(), "No llamar a CeroPapel antes del commit");
            if (rollback) status.setRollbackOnly();
        });
    }

    @Test void enviaSoloDespuesDelCommitYConservaIdentificadorExterno() {
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals(4, estado.llamadas.size());
            assertTrue(estado.cuerpos.get(1).contains("\"rut\":111"));
            assertTrue(estado.cuerpos.get(3).contains("\"perf_id\":123"));
            assertTrue(estado.cuerpos.get(3).contains("\"rut\":222"));
            assertTrue(estado.cuerpos.get(3).contains(subrogancia.getFechaInicio() + " 00:00:00"));
            assertTrue(estado.cuerpos.get(3).contains(subrogancia.getFechaFin() + " 23:59:59"));
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
            assertEquals(456L, subrogancia.getCeroPapelTareaId());
            assertEquals(123L, subrogancia.getCeroPapelPerfilId());
            ctx.getBean(CeroPapelSubroganciaService.class).enviar(new SubroganciaCeroPapelEvent(1L));
            assertEquals(4, estado.llamadas.size(), "No reenviar una operación procesada");
        }
    }

    @Test void rollbackNoEnvia() {
        try (var ctx = crearContexto()) {
            publicar(ctx, true);
            assertTrue(estado.llamadas.isEmpty());
            assertEquals(0, estado.guardados);
        }
    }

    @Test void multiplesPerfilesSinRelacionNoActivan() {
        estado.perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals(2, estado.llamadas.size());
            assertEquals("PENDIENTE_PERFIL", subrogancia.getCeroPapelEstado());
        }
    }

    @Test void perfilConfiguradoSeleccionaCargoCorrecto() {
        estado.perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        try (var ctx = crearContexto()) {
            ctx.getBean(CeroPapelProperties.class).getPerfilesPorDepartamento().put(10L, 124L);
            publicar(ctx, false);
            assertEquals(124L, subrogancia.getCeroPapelPerfilId());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
        }
    }

    @Test void errorExternoNoDeshaceRegistroLocal() {
        estado.activacion = "{\"estado\":\"ERROR\",\"msg\":\"Período superpuesto\"}";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
            assertEquals(1, estado.guardados);
        }
    }

    @Test void inicioHoyNoSeConvierteEnReemplazoIndefinido() {
        subrogancia.setFechaInicio(FechaUtils.fechaActual());
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertTrue(estado.llamadas.isEmpty());
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
        }
    }
    private SubroganciaRequest solicitudManual(boolean enviar) {
        var request = new SubroganciaRequest();
        request.setRutJefe(111);
        request.setRutSubrogante(222);
        request.setGrabarCeroPapel(enviar);
        return request;
    }

    @Test void altaSinOpcionExplicitaTambienEnvia() {
        try (var ctx = crearContexto()) {
            ctx.getBean(SubroganciaService.class)
                .createSubrogancia(solicitudManual(false), subrogancia.getFechaInicio(), subrogancia.getFechaFin(), 10L);
            assertEquals(4, estado.llamadas.size());
            assertEquals("ENVIADA", estado.registro.getCeroPapelEstado());
            assertEquals(2, estado.guardados);
        }
    }

    @Test void mantenedorConOpcionSiEnvia() {
        try (var ctx = crearContexto()) {
            ctx.getBean(SubroganciaService.class)
                .createSubrogancia(solicitudManual(true), subrogancia.getFechaInicio(), subrogancia.getFechaFin(), 10L);
            assertEquals(4, estado.llamadas.size());
            assertEquals("ENVIADA", estado.registro.getCeroPapelEstado());
        }
    }

    @Test void solicitudGuardaPrimeroYEnviaTrasCommitDeLaTransaccionExterior() {
        try (var ctx = crearContexto()) {
            new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)).executeWithoutResult(status -> {
                ctx.getBean(SubroganciaService.class)
                    .createSubrogancia(solicitudManual(false), subrogancia.getFechaInicio(), subrogancia.getFechaFin(), 10L);
                assertNotNull(estado.registro.getId());
                assertEquals("PENDIENTE_ENVIO", estado.registro.getCeroPapelEstado());
                assertEquals(1, estado.guardados);
                assertTrue(estado.llamadas.isEmpty(), "La API debe esperar al commit de la solicitud");
            });
            assertEquals("ENVIADA", estado.registro.getCeroPapelEstado());
        }
    }

    @Test void altaConRechazoExternoConservaRegistroYEscribeLog() {
        estado.activacion = "{\"estado\":\"ERROR\",\"msg\":\"Período superpuesto\"}";
        var logger = (Logger) LoggerFactory.getLogger(CeroPapelSubroganciaService.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try (var ctx = crearContexto()) {
            var servicio = ctx.getBean(SubroganciaService.class);
            var request = solicitudManual(false);
            var inicio = subrogancia.getFechaInicio();
            var fin = subrogancia.getFechaFin();
            Long id = assertDoesNotThrow(() -> servicio.createSubrogancia(request, inicio, fin, 10L));
            assertEquals(estado.registro.getId(), id);
            assertEquals("ERROR", estado.registro.getCeroPapelEstado());
            assertTrue(estado.registro.getCeroPapelDetalle().contains("Período superpuesto"));
            assertEquals(2, estado.guardados, "Alta local y actualización del resultado externo");
            assertTrue(appender.list.stream().anyMatch(event ->
                event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("subroganciaLocal=" + id)));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @ParameterizedTest
    @EnumSource(SubroganciaCeroPapelEvent.Origen.class)
    void logRegistraInicioYResultadoSinCredenciales(SubroganciaCeroPapelEvent.Origen origen) {
        var logger = (Logger) LoggerFactory.getLogger(CeroPapelSubroganciaService.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            estado.registro.setCeroPapelEstado("PENDIENTE_ENVIO");
            try (var ctx = crearContexto()) {
                new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)).executeWithoutResult(
                    status -> ctx.publishEvent(new SubroganciaCeroPapelEvent(1L, origen)));
            }
            var mensajes = appender.list.stream().map(
                ILoggingEvent::getFormattedMessage).toList();
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("envio inicio subroganciaLocal=1 origen=" + origen)));
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("origen=" + origen + " estado=ENVIADA")));
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("api inicio") && m.contains("origen=" + origen)));
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("api respuesta") && m.contains("origen=" + origen)));
            assertTrue(mensajes.stream().noneMatch(m -> m.contains("test-token") || m.contains("test-password")));
            assertNull(MDC.get("ceroPapelOrigen"), "El contexto no debe filtrarse a otra operación");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test void errorHttpTambienDejaInicioYResultadoDeLaLlamada() {
        estado.httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        var logger = (Logger) LoggerFactory.getLogger(CeroPapelSubroganciaService.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals("ERROR", estado.registro.getCeroPapelEstado());
            var mensajes = appender.list.stream().map(
                ILoggingEvent::getFormattedMessage).toList();
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("api inicio") && m.contains("autenticacion.php")));
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("api error") && m.contains("http=503")));
            assertTrue(mensajes.stream().anyMatch(m -> m.contains("envio fin") && m.contains("estado=ERROR")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test void desactivarConservaAltaLocalSinLlamadasYReactivarNoLaReenvia() {
        try (var ctx = crearContexto()) {
            var properties = ctx.getBean(CeroPapelProperties.class);
            properties.setHabilitada(false);
            ctx.getBean(SubroganciaService.class).createSubrogancia(solicitudManual(false),
                    subrogancia.getFechaInicio(), subrogancia.getFechaFin(), 10L,
                    SubroganciaCeroPapelEvent.Origen.AUTOMATICO_SOLICITUD);
            assertNotNull(estado.registro.getId());
            assertEquals("DESACTIVADA", estado.registro.getCeroPapelEstado());
            assertTrue(estado.llamadas.isEmpty());
            properties.setHabilitada(true);
            publicar(ctx, false);
            assertTrue(estado.llamadas.isEmpty(), "Reactivar no debe reenviar altas desactivadas");
        }
    }

    @Test void desactivarDuranteEnvioImpideLaSiguienteLlamada() {
        estado.desactivarTrasAutenticacion = true;
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals(1, estado.llamadas.size());
            assertEquals("DESACTIVADA", estado.registro.getCeroPapelEstado());
        }
    }

    @Test void configuracionNuevaSeAplicaSinRecrearElServicio() {
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            var properties = ctx.getBean(CeroPapelProperties.class);
            properties.setBaseUrl("https://otro.demo.ceropapel.cl/inter/");
            properties.setPassword("nueva-clave-de-prueba");
            properties.setUsuario("otro-usuario");
            estado.registro.setCeroPapelEstado("PENDIENTE_ENVIO");
            publicar(ctx, false);
            assertEquals("otro.demo.ceropapel.cl", estado.destinos.get(4));
            assertTrue(estado.cuerpos.get(4).contains("nueva-clave-de-prueba"));
            assertTrue(estado.cuerpos.get(4).contains("otro-usuario"));
            assertEquals("ENVIADA", estado.registro.getCeroPapelEstado());
        }
    }

    @Test void seleccionConfirmadaSeGuardaYPermiteEnviarRegistroAntesBloqueado() {
        estado.perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        subrogancia.setCeroPapelEstado("ERROR");
        try (var ctx = crearContexto()) {
            var service = ctx.getBean(CeroPapelSubroganciaService.class);
            service.confirmarPerfil(1L, 124L);
            assertEquals(124L, estado.relaciones.get(10L).getPerfilId());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
            assertEquals(124L, subrogancia.getCeroPapelPerfilId());
            int cantidad = estado.llamadas.size();
            assertThrows(SubroganciaException.class, () -> service.confirmarPerfil(1L, 124L));
            assertEquals(cantidad, estado.llamadas.size());
        }
    }

    @Test void cargoFueraDeLosPerfilesDelJefeNoSeGuardaNiActiva() {
        subrogancia.setCeroPapelEstado("PENDIENTE_PERFIL");
        try (var ctx = crearContexto()) {
            var service = ctx.getBean(CeroPapelSubroganciaService.class);
            assertThrows(SubroganciaException.class, () -> service.confirmarPerfil(1L, 999L));
            assertTrue(estado.relaciones.isEmpty());
            assertEquals("PENDIENTE_PERFIL", subrogancia.getCeroPapelEstado());
            assertEquals(2, estado.llamadas.size());
        }
    }

    @Test void resultadoInciertoNoPermiteReenviar() {
        subrogancia.setCeroPapelEstado("ERROR");
        subrogancia.setCeroPapelPerfilId(123L);
        try (var ctx = crearContexto()) {
            var service = ctx.getBean(CeroPapelSubroganciaService.class);
            assertThrows(SubroganciaException.class, () -> service.confirmarPerfil(1L, 123L));
            assertTrue(estado.llamadas.isEmpty());
        }
    }

    @Test void relacionConfirmadaSeReutilizaYSeValidaContraPerfilesActivos() {
        estado.relaciones.put(10L, new PerfilDepartamentoCeroPapel(
            10L, 124L, "http://laflorida.demo.ceropapel.cl/inter/"));
        estado.perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals(124L, subrogancia.getCeroPapelPerfilId());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
        }
    }

    @Test void relacionObsoletaRequiereNuevaConfirmacion() {
        estado.relaciones.put(10L, new PerfilDepartamentoCeroPapel(
            10L, 999L, "http://laflorida.demo.ceropapel.cl/inter/"));
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals("PENDIENTE_PERFIL", subrogancia.getCeroPapelEstado());
            assertEquals(2, estado.llamadas.size());
        }
    }

    @Test void admiteJsonEtiquetadoComoHtmlPorElDemo() {
        estado.tipoRespuesta = "text/html;charset=UTF-8";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals(4, estado.llamadas.size());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
            assertEquals(456L, subrogancia.getCeroPapelTareaId());
        }
    }

    @Test void htmlRealNoSeInterpretaComoProgramacionExitosa() {
        estado.tipoRespuesta = "text/html;charset=UTF-8";
        estado.activacion = "<html>Error del servidor</html>";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
            assertNull(subrogancia.getCeroPapelTareaId());
        }
    }

    @Test void subroganteSinPerfilNoLlamaActivacionYConservaMotivo() {
        estado.perfilesSubrogante = "{\"estado\":\"OK\",\"perfiles\":[]}";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals("SIN_PERFIL_SUBROGANTE", subrogancia.getCeroPapelEstado());
            assertTrue(subrogancia.getCeroPapelDetalle().contains("no tiene un perfil activo"));
            assertNull(subrogancia.getCeroPapelPerfilId());
            assertEquals(3, estado.llamadas.size());
            assertTrue(estado.llamadas.stream().noneMatch(p -> p.endsWith("activarSubrogancia.php")));
        }
    }

    @Test void perfilesActivosNoGarantizanMismaInstitucionYSeConservaRechazoReal() {
        estado.activacion = "{\"estado\":\"ERROR\",\"msg\":\"El usuario subrogante no tiene un perfil activo en la institución del perfil a subrogar\"}";
        try (var ctx = crearContexto()) {
            publicar(ctx, false);
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
            assertTrue(subrogancia.getCeroPapelDetalle().contains("institución del perfil a subrogar"));
            assertEquals(4, estado.llamadas.size());
            assertNull(subrogancia.getCeroPapelTareaId());
        }
    }

}
