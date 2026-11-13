package com.newsolicitudes.newsolicitudes.services.departamento;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;

/** Verifica la cookie en el servicio de autenticación; nunca acepta un RUT enviado como autorización. */
@Service
public class PermisoAsociacionDepartamentoService {
    private final WebClient autenticacion;
    private final UsuarioRepository usuarios;

    public PermisoAsociacionDepartamentoService(WebClient.Builder builder, UsuarioRepository usuarios,
            @Value("${api.auth-url:https://intranet.laflorida.cl}") String authUrl) {
        this.autenticacion = builder.baseUrl(authUrl).build();
        this.usuarios = usuarios;
    }

    public record Identidad(String rut) { }
    public record Sesion(Boolean success, Identidad data) { }

    public boolean puedeAdministrar(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) token = cookie.getValue();
            }
        }
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicie sesión para administrar departamentos.");
        }
        Sesion sesion;
        try {
            sesion = autenticacion.get().uri("/api/auth/me-funcionarios").cookie("token", token)
                    .retrieve().bodyToMono(Sesion.class).timeout(Duration.ofSeconds(10)).block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException error) {
            if (error.getStatusCode().value() == 401 || error.getStatusCode().value() == 403) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión no es válida.");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo validar la sesión.");
        } catch (RuntimeException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo validar la sesión.");
        }
        if (sesion == null || !Boolean.TRUE.equals(sesion.success()) || sesion.data() == null
                || sesion.data().rut() == null || !sesion.data().rut().matches("[0-9.]+(?:-[0-9kK])?")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión no es válida.");
        }
        String rut = sesion.data().rut().replace(".", "").split("-")[0];
        return usuarios.findByRut(rut).stream().flatMap(u -> u.getModulos().stream())
                .anyMatch(m -> "ADMINISTRACION".equals(m.getNombre()) || "PARAMETROS".equals(m.getNombre()));
    }

    public void exigirAdministrador(HttpServletRequest request) {
        if (!"1".equals(request.getHeader("X-Departamento-Admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Petición de asociación no válida.");
        }
        if (!puedeAdministrar(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para asociar departamentos.");
        }
    }
}
