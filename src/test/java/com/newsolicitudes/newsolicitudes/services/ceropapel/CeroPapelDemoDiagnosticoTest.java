package com.newsolicitudes.newsolicitudes.services.ceropapel;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static org.junit.jupiter.api.Assertions.*;

/** Diagnóstico opt-in: solo autenticación y consulta de perfiles, nunca activación. */
@EnabledIfEnvironmentVariable(named = "RUN_CEROPAPEL_DEMO_DIAGNOSTICO", matches = "true")
class CeroPapelDemoDiagnosticoTest {
    @Test void autenticarYConsultarConElMismoClienteDelBackend() {
        WebClient client = WebClient.builder().baseUrl("http://laflorida.demo.ceropapel.cl/inter/").build();
        Map<String, Object> auth = llamar(client, "autenticacion.php", Map.of(
            "usuario", "app_test", "pass", System.getenv("CEROPAPEL_PASSWORD")));
        assertEquals("OK", auth.get("estado"));
        assertNotNull(auth.get("token"));
        Map<String, Object> perfiles = llamar(client, "getPerfilesRUT.php", Map.of(
            "token", auth.get("token"), "rut", 10397956));
        assertEquals("OK", perfiles.get("estado"));
    }
    private Map<String, Object> llamar(WebClient client, String endpoint, Map<String, Object> body) {
        try {
            String respuesta = client.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body).retrieve().bodyToMono(String.class).block(Duration.ofSeconds(30));
            var result = CeroPapelSubroganciaService.leerRespuesta(endpoint, respuesta);
            System.out.println("Diagnóstico demo " + endpoint + " HTTP 200 estado=" + result.get("estado"));
            return result;
        } catch (WebClientResponseException e) {
            System.out.println("Diagnóstico demo " + endpoint + " HTTP " + e.getStatusCode().value()
                + " tipo=" + e.getHeaders().getContentType());
            // No imprimir respuesta completa, porque podría contener credenciales o tokens.
            throw new AssertionError("HTTP " + e.getStatusCode().value() + " en " + endpoint);
        }
    }
}
