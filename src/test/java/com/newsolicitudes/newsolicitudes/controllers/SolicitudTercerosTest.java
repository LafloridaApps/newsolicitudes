package com.newsolicitudes.newsolicitudes.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.entities.Usuario;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudService;

class SolicitudTercerosTest {
    private final SolicitudService servicio = mock(SolicitudService.class, withSettings().mockMaker("mock-maker-proxy"));
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class, withSettings().mockMaker("mock-maker-proxy"));
    private final com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudTercerosService terceros =
            mock(com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudTercerosService.class, withSettings().mockMaker("mock-maker-proxy"));
    private final SolicitudController controller = new SolicitudController(servicio, usuarios, terceros);

    @Test
    void rechazaUsuarioSinPermiso() {
        when(usuarios.findByRut("123")).thenReturn(Optional.of(new Usuario()));
        assertEquals(403, controller.crearSolicitudTercero(new SolicitudRequest(), "123").getStatusCode().value());
        verifyNoInteractions(servicio);
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(usuarios.findByRut("123")).thenReturn(Optional.empty());
        assertEquals(403, controller.crearSolicitudTercero(new SolicitudRequest(), "123").getStatusCode().value());
        verifyNoInteractions(servicio);
    }

    @Test
    void autorizadoUsaElMismoFlujoConElSolicitanteOriginal() {
        Usuario gestor = new Usuario();
        gestor.setModulos(Set.of(new Modulo("SOLICITUD POR TERCEROS")));
        when(usuarios.findByRut("123")).thenReturn(Optional.of(gestor));
        SolicitudRequest solicitud = new SolicitudRequest();
        solicitud.setRut(456);
        solicitud.setRutIngresadoPor(999);
        assertEquals(201, controller.crearSolicitudTercero(solicitud, "123").getStatusCode().value());
        verify(servicio).createSolicitud(solicitud);
        assertEquals(456, solicitud.getRut());
        assertEquals(123, solicitud.getRutIngresadoPor());
        verify(terceros).validarSolicitante("123", 456);
    }

    @Test
    void solicitudPropiaRegistraAlMismoFuncionario() {
        SolicitudRequest solicitud = new SolicitudRequest();
        solicitud.setRut(456);
        solicitud.setRutIngresadoPor(999);
        controller.crearSolicitud(solicitud);
        assertEquals(456, solicitud.getRutIngresadoPor());
        verify(servicio).createSolicitud(solicitud);
    }

    @Test
    void mapperConservaBeneficiarioYAutor() {
        SolicitudRequest request = new SolicitudRequest();
        request.setTipoSolicitud("ADMINISTRATIVO");
        request.setRutIngresadoPor(123);
        var mapper = new com.newsolicitudes.newsolicitudes.mappers.SolicitudMapper();
        var solicitud = mapper.solicitudRequestToSolicitud(request, 456, 1L, 1);
        assertEquals(456, solicitud.getRut());
        assertEquals(123, solicitud.getRutIngresadoPor());
        request.setRutIngresadoPor(null);
        var propia = mapper.solicitudRequestToSolicitud(request, 456, 1L, 1);
        assertEquals(456, propia.getRutIngresadoPor());
    }
}
