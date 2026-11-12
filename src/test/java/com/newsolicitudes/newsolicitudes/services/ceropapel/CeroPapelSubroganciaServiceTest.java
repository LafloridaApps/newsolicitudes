package com.newsolicitudes.newsolicitudes.services.ceropapel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.*;
import com.newsolicitudes.newsolicitudes.config.CeroPapelProperties;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

class CeroPapelSubroganciaServiceTest {
    static final List<String> llamadas = new ArrayList<>();
    static final List<String> cuerpos = new ArrayList<>();
    static String perfiles;
    static String perfilesSubrogante;
    static String activacion;
    static String tipoRespuesta;
    private Subrogancia subrogancia;
    static Subrogancia registro;
    static int guardados;
    static final java.util.Map<Long, com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel> relaciones = new java.util.HashMap<>();

    @org.springframework.boot.test.context.TestConfiguration
    @EnableTransactionManagement
    @Import({CeroPapelSubroganciaService.class, com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaServiceImpl.class})
    static class Config {
        @Bean CeroPapelProperties properties() {
            CeroPapelProperties p = new CeroPapelProperties();
            p.setPassword("test-password");
            return p;
        }
        @Bean SubroganciaRepository repository() {
            return (SubroganciaRepository) java.lang.reflect.Proxy.newProxyInstance(
                SubroganciaRepository.class.getClassLoader(), new Class<?>[] {SubroganciaRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById", "findForUpdate" -> Optional.ofNullable(registro);
                    case "save" -> {
                        guardados++;
                        registro = (Subrogancia) args[0];
                        if (registro.getId() == null) registro.setId(1L);
                        yield registro;
                    }
                    case "existeSubrogancia" -> Optional.empty();
                    case "toString" -> "TestSubroganciaRepository";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        }
        @Bean com.newsolicitudes.newsolicitudes.repositories.PerfilDepartamentoCeroPapelRepository relacionesRepository() {
            return (com.newsolicitudes.newsolicitudes.repositories.PerfilDepartamentoCeroPapelRepository)
                java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {com.newsolicitudes.newsolicitudes.repositories.PerfilDepartamentoCeroPapelRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findById" -> Optional.ofNullable(relaciones.get(args[0]));
                        case "save" -> {
                            var r = (com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel) args[0];
                            relaciones.put(r.getIdDepto(), r);
                            yield r;
                        }
                        case "toString" -> "RelacionesTest";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
        @Bean com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService departamentos() {
            return (com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService)
                java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("getDepartamentoById")) {
                            var d = new com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse();
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
        @Bean WebClient.Builder builder() {
            return WebClient.builder().exchangeFunction(request -> {
                String endpoint = request.url().getPath();
                llamadas.add(endpoint);
                String body = endpoint.endsWith("autenticacion.php")
                    ? "{\"estado\":\"OK\",\"token\":\"test-token\"}"
                    : endpoint.endsWith("getPerfilesRUT.php") ? perfiles : activacion;
                var salida = new org.springframework.mock.http.client.reactive.MockClientHttpRequest(request.method(), request.url());
                return request.writeTo(salida, ExchangeStrategies.withDefaults())
                    .then(reactor.core.publisher.Mono.defer(salida::getBodyAsString)).map(json -> {
                        cuerpos.add(json);
                        String respuesta = endpoint.endsWith("getPerfilesRUT.php") && json.contains("\"rut\":222")
                            ? perfilesSubrogante : body;
                        return ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", tipoRespuesta).body(respuesta).build();
                    });
            });
        }
    }

    @BeforeEach void preparar() {
        llamadas.clear();
        cuerpos.clear();
        guardados = 0;
        tipoRespuesta = MediaType.APPLICATION_JSON_VALUE;
        perfilesSubrogante = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":999}]}";
        relaciones.clear();
        perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":\"123\",\"uni_nombre\":\"TECNOLOGIAS DE LA INFORMACION\"}]}";
        activacion = "{\"estado\":\"OK\",\"modo\":\"programada\",\"tare_id\":456}";
        subrogancia = new Subrogancia(222, FechaUtils.fechaActual().plusDays(2),
                FechaUtils.fechaActual().plusDays(3), 111, 10L);
        subrogancia.setId(1L);
        registro = subrogancia;
    }

    private void publicar(AnnotationConfigApplicationContext ctx, boolean rollback) {
        new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)).executeWithoutResult(status -> {
            ctx.publishEvent(new SubroganciaCeroPapelEvent(1L));
            assertTrue(llamadas.isEmpty(), "No llamar a CeroPapel antes del commit");
            if (rollback) status.setRollbackOnly();
        });
    }

    @Test void enviaSoloDespuesDelCommitYConservaIdentificadorExterno() {
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals(4, llamadas.size());
            assertTrue(cuerpos.get(1).contains("\"rut\":111"));
            assertTrue(cuerpos.get(3).contains("\"perf_id\":123"));
            assertTrue(cuerpos.get(3).contains("\"rut\":222"));
            assertTrue(cuerpos.get(3).contains(subrogancia.getFechaInicio() + " 00:00:00"));
            assertTrue(cuerpos.get(3).contains(subrogancia.getFechaFin() + " 23:59:59"));
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
            assertEquals(456L, subrogancia.getCeroPapelTareaId());
            assertEquals(123L, subrogancia.getCeroPapelPerfilId());
            ctx.getBean(CeroPapelSubroganciaService.class).enviar(new SubroganciaCeroPapelEvent(1L));
            assertEquals(4, llamadas.size(), "No reenviar una operación procesada");
        }
    }

    @Test void rollbackNoEnvia() {
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, true);
            assertTrue(llamadas.isEmpty());
            assertEquals(0, guardados);
        }
    }

    @Test void multiplesPerfilesSinRelacionNoActivan() {
        perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals(2, llamadas.size());
            assertEquals("PENDIENTE_PERFIL", subrogancia.getCeroPapelEstado());
        }
    }

    @Test void perfilConfiguradoSeleccionaCargoCorrecto() {
        perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            ctx.getBean(CeroPapelProperties.class).getPerfilesPorDepartamento().put(10L, 124L);
            publicar(ctx, false);
            assertEquals(124L, subrogancia.getCeroPapelPerfilId());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
        }
    }

    @Test void errorExternoNoDeshaceRegistroLocal() {
        activacion = "{\"estado\":\"ERROR\",\"msg\":\"Período superpuesto\"}";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
            assertEquals(1, guardados);
        }
    }

    @Test void inicioHoyNoSeConvierteEnReemplazoIndefinido() {
        subrogancia.setFechaInicio(FechaUtils.fechaActual());
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertTrue(llamadas.isEmpty());
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
        }
    }
    private com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest solicitudManual(boolean enviar) {
        var request = new com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest();
        request.setRutJefe(111);
        request.setRutSubrogante(222);
        request.setGrabarCeroPapel(enviar);
        return request;
    }

    @Test void mantenedorSoloLocalNoLlamaApi() {
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            ctx.getBean(com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService.class)
                .createSubrogancia(solicitudManual(false), subrogancia.getFechaInicio(), subrogancia.getFechaFin(), 10L);
            assertTrue(llamadas.isEmpty());
            assertEquals(1, guardados);
        }
    }

    @Test void mantenedorConOpcionSiEnvia() {
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            ctx.getBean(com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService.class)
                .createSubrogancia(solicitudManual(true), subrogancia.getFechaInicio(), subrogancia.getFechaFin(), 10L);
            assertEquals(4, llamadas.size());
            assertEquals("ENVIADA", registro.getCeroPapelEstado());
        }
    }

    @Test void seleccionConfirmadaSeGuardaYPermiteEnviarRegistroAntesBloqueado() {
        perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        subrogancia.setCeroPapelEstado("ERROR");
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            var service = ctx.getBean(CeroPapelSubroganciaService.class);
            service.confirmarPerfil(1L, 124L);
            assertEquals(124L, relaciones.get(10L).getPerfilId());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
            assertEquals(124L, subrogancia.getCeroPapelPerfilId());
            int cantidad = llamadas.size();
            assertThrows(RuntimeException.class, () -> service.confirmarPerfil(1L, 124L));
            assertEquals(cantidad, llamadas.size());
        }
    }

    @Test void cargoFueraDeLosPerfilesDelJefeNoSeGuardaNiActiva() {
        subrogancia.setCeroPapelEstado("PENDIENTE_PERFIL");
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            var service = ctx.getBean(CeroPapelSubroganciaService.class);
            assertThrows(RuntimeException.class, () -> service.confirmarPerfil(1L, 999L));
            assertTrue(relaciones.isEmpty());
            assertEquals("PENDIENTE_PERFIL", subrogancia.getCeroPapelEstado());
            assertEquals(2, llamadas.size());
        }
    }

    @Test void resultadoInciertoNoPermiteReenviar() {
        subrogancia.setCeroPapelEstado("ERROR");
        subrogancia.setCeroPapelPerfilId(123L);
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            var service = ctx.getBean(CeroPapelSubroganciaService.class);
            assertThrows(RuntimeException.class, () -> service.confirmarPerfil(1L, 123L));
            assertTrue(llamadas.isEmpty());
        }
    }

    @Test void relacionConfirmadaSeReutilizaYSeValidaContraPerfilesActivos() {
        relaciones.put(10L, new com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel(
            10L, 124L, "http://laflorida.demo.ceropapel.cl/inter/"));
        perfiles = "{\"estado\":\"OK\",\"perfiles\":[{\"codigo\":123},{\"codigo\":124}]}";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals(124L, subrogancia.getCeroPapelPerfilId());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
        }
    }

    @Test void relacionObsoletaRequiereNuevaConfirmacion() {
        relaciones.put(10L, new com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel(
            10L, 999L, "http://laflorida.demo.ceropapel.cl/inter/"));
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals("PENDIENTE_PERFIL", subrogancia.getCeroPapelEstado());
            assertEquals(2, llamadas.size());
        }
    }

    @Test void admiteJsonEtiquetadoComoHtmlPorElDemo() {
        tipoRespuesta = "text/html;charset=UTF-8";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals(4, llamadas.size());
            assertEquals("ENVIADA", subrogancia.getCeroPapelEstado());
            assertEquals(456L, subrogancia.getCeroPapelTareaId());
        }
    }

    @Test void htmlRealNoSeInterpretaComoProgramacionExitosa() {
        tipoRespuesta = "text/html;charset=UTF-8";
        activacion = "<html>Error del servidor</html>";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
            assertNull(subrogancia.getCeroPapelTareaId());
        }
    }

    @Test void subroganteSinPerfilNoLlamaActivacionYConservaMotivo() {
        perfilesSubrogante = "{\"estado\":\"OK\",\"perfiles\":[]}";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals("SIN_PERFIL_SUBROGANTE", subrogancia.getCeroPapelEstado());
            assertTrue(subrogancia.getCeroPapelDetalle().contains("no tiene un perfil activo"));
            assertNull(subrogancia.getCeroPapelPerfilId());
            assertEquals(3, llamadas.size());
            assertTrue(llamadas.stream().noneMatch(p -> p.endsWith("activarSubrogancia.php")));
        }
    }

    @Test void perfilesActivosNoGarantizanMismaInstitucionYSeConservaRechazoReal() {
        activacion = "{\"estado\":\"ERROR\",\"msg\":\"El usuario subrogante no tiene un perfil activo en la institución del perfil a subrogar\"}";
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            publicar(ctx, false);
            assertEquals("ERROR", subrogancia.getCeroPapelEstado());
            assertTrue(subrogancia.getCeroPapelDetalle().contains("institución del perfil a subrogar"));
            assertEquals(4, llamadas.size());
            assertNull(subrogancia.getCeroPapelTareaId());
        }
    }

}
