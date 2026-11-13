package com.newsolicitudes.newsolicitudes.services.departamento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import jakarta.servlet.http.Cookie;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import com.newsolicitudes.newsolicitudes.entities.Usuario;
import com.newsolicitudes.newsolicitudes.entities.Modulo;

class PermisoAsociacionDepartamentoTest {
    @Test
    void rutGestorEnQueryNoAutorizaSinSesion() {
        var usuarios = mock(UsuarioRepository.class);
        var service = new PermisoAsociacionDepartamentoService(WebClient.builder(), usuarios, "http://auth.test");
        var request = new MockHttpServletRequest(); request.addParameter("rutGestor", "123");
        request.addHeader("X-Departamento-Admin", "1");
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> service.exigirAdministrador(request)).getStatusCode().value());
        verifyNoInteractions(usuarios);
    }

    @Test
    void sesionSinPermisosNoPuedeGuardar() {
        var usuarios = mock(UsuarioRepository.class);
        var service = servicioConSesion(usuarios);
        when(usuarios.findByRut("123")).thenReturn(Optional.of(new Usuario("123", "usuario", Set.of())));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.exigirAdministrador(request())).getStatusCode().value());
    }

    @Test
    void administradorSeObtieneDeSesionValidadaYNoDelRutEnviado() {
        var usuarios = mock(UsuarioRepository.class);
        var modulo = new Modulo(); modulo.setNombre("ADMINISTRACION");
        when(usuarios.findByRut("123")).thenReturn(Optional.of(new Usuario("123", "admin", Set.of(modulo))));
        var service = servicioConSesion(usuarios);
        var request = request(); request.addParameter("rutGestor", "999");
        assertDoesNotThrow(() -> service.exigirAdministrador(request));
        verify(usuarios).findByRut("123"); verify(usuarios, never()).findByRut("999");
    }

    private MockHttpServletRequest request() {
        var request = new MockHttpServletRequest(); request.setCookies(new Cookie("token", "sesion"));
        request.addHeader("X-Departamento-Admin", "1"); return request;
    }

    private PermisoAsociacionDepartamentoService servicioConSesion(UsuarioRepository usuarios) {
        ExchangeFunction exchange = request -> {
            assertEquals("token=sesion", request.cookies().toSingleValueMap().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue()).findFirst().orElseThrow());
            return Mono.just(ClientResponse.create(HttpStatus.OK).header("Content-Type", "application/json")
                    .body("{\"success\":true,\"data\":{\"rut\":\"123\"}}").build());
        };
        return new PermisoAsociacionDepartamentoService(WebClient.builder().exchangeFunction(exchange), usuarios, "http://auth.test");
    }
}
