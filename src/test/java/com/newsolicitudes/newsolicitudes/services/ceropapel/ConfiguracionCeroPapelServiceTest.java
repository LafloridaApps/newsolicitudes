package com.newsolicitudes.newsolicitudes.services.ceropapel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.config.CeroPapelProperties;
import com.newsolicitudes.newsolicitudes.dto.ConfiguracionCeroPapelRequest;
import com.newsolicitudes.newsolicitudes.repositories.ConfiguracionCeroPapelRepository;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:configuracion;DB_CLOSE_DELAY=-1",
    "ceropapel.encryption-key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "ceropapel.password=clave-inicial-prueba",
    "ceropapel.base-url=http://laflorida.demo.ceropapel.cl/inter/"
})
class ConfiguracionCeroPapelServiceTest {
    @Autowired private ConfiguracionCeroPapelService service;
    @Autowired private ConfiguracionCeroPapelRepository repository;
    @Autowired private CeroPapelClaveService claves;
    @Autowired private CeroPapelProperties properties;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        service.inicializar();
    }

    @Test
    void cambiosPersistidosConservanClaveSiSeDejaVacia() {
        var inicial = service.consultar();
        var request = new ConfiguracionCeroPapelRequest(false, inicial.url(), "otro", "", inicial.version());
        var respuesta = service.guardar(request);
        assertFalse(service.estaHabilitada());
        assertEquals("clave-inicial-prueba", claves.descifrar(repository.findById(1L).orElseThrow().getClaveCifrada()));
        assertEquals("otro", service.obtenerConexion().usuario());
        assertTrue(respuesta.claveConfigurada());
        assertTrue(respuesta.version() > inicial.version());
        service.inicializar();
        assertFalse(service.estaHabilitada(), "Reiniciar no debe reemplazar la configuración guardada");
    }

    @Test
    void claveActualizadaSeCifraYNoSeExponeEnLaRespuesta() {
        var inicial = service.consultar();
        var request = new ConfiguracionCeroPapelRequest(true, inicial.url(), inicial.usuario(), "otra-clave", inicial.version());
        var respuesta = service.guardar(request);
        assertEquals("otra-clave", service.obtenerConexion().clave());
        byte[] guardada = repository.findById(1L).orElseThrow().getClaveCifrada();
        assertEquals("otra-clave", claves.descifrar(guardada));
        assertFalse(respuesta.toString().contains("otra-clave"));
        assertFalse(request.toString().contains("otra-clave"));
        assertFalse(service.obtenerConexion().toString().contains("otra-clave"));
    }

    @Test
    void rechazaVersionAntiguaSinSobrescribir() {
        var inicial = service.consultar();
        var request = new ConfiguracionCeroPapelRequest(false, inicial.url(), inicial.usuario(), "", inicial.version());
        service.guardar(request);
        var error = assertThrows(ResponseStatusException.class, () -> service.guardar(request));
        assertEquals(409, error.getStatusCode().value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost/inter/", "http://127.0.0.1/", "file:///tmp/clave",
        "http://laflorida.demo.ceropapel.cl@localhost/inter/",
        "http://laflorida.demo.ceropapel.cl/inter/?url=http://localhost",
        "http://laflorida.demo.ceropapel.cl:8080/inter/"
    })
    void rechazaDestinosNoPermitidos(String url) {
        var inicial = service.consultar();
        var request = new ConfiguracionCeroPapelRequest(true, url, inicial.usuario(), "", inicial.version());
        var error = assertThrows(ResponseStatusException.class, () -> service.guardar(request));
        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void noActivaSinClavePrevia() {
        var config = repository.findById(1L).orElseThrow();
        config.setClaveCifrada(new byte[0]);
        repository.saveAndFlush(config);
        var inicial = service.consultar();
        var request = new ConfiguracionCeroPapelRequest(true, inicial.url(), inicial.usuario(), "", inicial.version());
        var error = assertThrows(ResponseStatusException.class, () -> service.guardar(request));
        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void cifradoEsAleatorioYDetectaAlteraciones() {
        byte[] primero = claves.cifrar("prueba");
        byte[] segundo = claves.cifrar("prueba");
        assertFalse(Arrays.equals(primero, segundo));
        primero[primero.length - 1] ^= 1;
        assertThrows(IllegalStateException.class, () -> claves.descifrar(primero));
        assertFalse(properties.getEncryptionKey().isBlank());
    }
    @Test
    void instalacionSinCredencialesArrancaDesactivada() {
        String url = properties.getBaseUrl();
        String clave = properties.getPassword();
        try {
            properties.setBaseUrl("");
            properties.setPassword("");
            repository.deleteAll();
            service.inicializar();
            assertFalse(service.estaHabilitada());
            assertFalse(service.consultar().claveConfigurada());
            assertEquals("", service.consultar().url());
        } finally {
            properties.setBaseUrl(url);
            properties.setPassword(clave);
        }
    }

    private org.springframework.test.web.servlet.MockMvc controlador(boolean conPermiso) {
        var usuarios = org.mockito.Mockito.mock(com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository.class);
        if (conPermiso) {
            var usuario = new com.newsolicitudes.newsolicitudes.entities.Usuario("12345678", "prueba",
                    java.util.Set.of(new com.newsolicitudes.newsolicitudes.entities.Modulo(ConfiguracionCeroPapelService.MODULO)));
            org.mockito.Mockito.when(usuarios.findByRut("12345678")).thenReturn(java.util.Optional.of(usuario));
        }
        var permisos = new CeroPapelPermisoService(usuarios);
        return org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                new com.newsolicitudes.newsolicitudes.controllers.CeroPapelConfiguracionController(service, permisos))
                .setControllerAdvice(new com.newsolicitudes.newsolicitudes.controllers.handlerexceptions.HandlerExceptions())
                .build();
    }

    @Test
    void valoresInicialesProvienenDePropertiesSinExponerLaClave() throws Exception {
        var valores = service.valoresIniciales();
        assertEquals(properties.getBaseUrl(), valores.url());
        assertEquals(properties.getUsuario(), valores.usuario());
        assertTrue(valores.claveConfigurada());
        controlador(false).perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/solicitudes/subrogancia/integracion/valores-iniciales"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.clave").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.claveConfigurada").value(true));
    }

    @Test
    void endpointNoExponeSecretosAlAdministrador() throws Exception {
        controlador(true).perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/solicitudes/subrogancia/integracion/configuracion").param("rutGestor", "12345678"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.clave").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.claveCifrada").doesNotExist())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.claveConfigurada").value(true));
    }

    @Test
    void revelarClaveExigePermisoYNoPermiteCache() throws Exception {
        var mvc = controlador(true);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/solicitudes/subrogancia/integracion/clave")
                .header("X-CeroPapel-Config", "1").param("rutGestor", "12345678"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Cache-Control", "no-store"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.clave").value("clave-inicial-prueba"));
        controlador(false).perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/solicitudes/subrogancia/integracion/clave").header("X-CeroPapel-Config", "1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        controlador(false).perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/solicitudes/subrogancia/integracion/clave")
                .header("X-CeroPapel-Config", "1").param("rutGestor", "12345678"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        assertFalse(service.consultarClave().toString().contains("clave-inicial-prueba"));
    }

    @Test
    void endpointExigeRutGestor() throws Exception {
        controlador(false).perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/solicitudes/subrogancia/integracion/configuracion").param("rut", "12345678"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void escrituraExigeCabeceraYValidaDatos() throws Exception {
        var mvc = controlador(true);
        String body = "{\"habilitada\":false,\"url\":\"http://laflorida.demo.ceropapel.cl/inter/\","
                + "\"usuario\":\"prueba\",\"clave\":\"\",\"version\":" + service.consultar().version() + "}";
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/solicitudes/subrogancia/integracion/configuracion").param("rutGestor", "12345678")
                .contentType("application/json").content(body))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/solicitudes/subrogancia/integracion/configuracion").param("rutGestor", "12345678")
                .header("X-CeroPapel-Config", "1").contentType("application/json").content(body))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/solicitudes/subrogancia/integracion/configuracion").param("rutGestor", "12345678")
                .header("X-CeroPapel-Config", "1").contentType("application/json").content("{}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

}
