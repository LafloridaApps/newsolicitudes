package com.newsolicitudes.newsolicitudes.services.ceropapel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.entities.Usuario;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;

class CeroPapelPermisoServiceTest {
    @Test
    void requiereRutGestorValido() {
        var usuarios = mock(UsuarioRepository.class);
        var service = new CeroPapelPermisoService(usuarios);
        var request = new MockHttpServletRequest();
        var error = assertThrows(ResponseStatusException.class, () -> service.exigirAdministrador(request));
        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(usuarios);
    }

    @Test
    void usuarioSinModuloNoPuedeAdministrar() {
        var usuarios = mock(UsuarioRepository.class);
        var request = new MockHttpServletRequest();
        request.setParameter("rutGestor", "12345678");
        when(usuarios.findByRut("12345678")).thenReturn(Optional.of(new Usuario("12345678", "prueba", Set.of())));
        var service = new CeroPapelPermisoService(usuarios);
        var error = assertThrows(ResponseStatusException.class, () -> service.exigirAdministrador(request));
        assertEquals(403, error.getStatusCode().value());
    }

    @Test
    void permiteUsuarioConModuloSinSesionDeIntranet() {
        var usuarios = mock(UsuarioRepository.class);
        var usuario = new Usuario("12345678", "prueba", Set.of(new Modulo(ConfiguracionCeroPapelService.MODULO)));
        when(usuarios.findByRut("12345678")).thenReturn(Optional.of(usuario));
        var request = new MockHttpServletRequest();
        request.setParameter("rutGestor", "12.345.678-5");
        var service = new CeroPapelPermisoService(usuarios);
        assertDoesNotThrow(() -> service.exigirAdministrador(request));
    }
}
