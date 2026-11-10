package com.newsolicitudes.newsolicitudes.services.firmante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import com.newsolicitudes.newsolicitudes.dto.*;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.*;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;

class FirmanteContratoTest {
    @Test
    void seleccionaSubdirectorSinConsultarContrato() {
        var departamentos = mock(ApiDepartamentoService.class, withSettings().mockMaker("mock-maker-proxy"));
        var subrogancias = mock(SubroganciaRepository.class, withSettings().mockMaker("mock-maker-proxy"));
        var aprobaciones = mock(AprobacionRepository.class, withSettings().mockMaker("mock-maker-proxy"));
        var servicio = new FirmanteServiceImpl(departamentos, subrogancias, aprobaciones);
        var solicitud = new Solicitud();
        solicitud.setRut(100); solicitud.setIdDepto(10L);
        var subdireccion = new DepartamentoResponse();
        subdireccion.setId(10L); subdireccion.setNivelDepartamento("SUBDIRECCION");
        subdireccion.setRutJefe(200); subdireccion.setIdDeptoSuperior(20L);
        var direccion = new DepartamentoResponse();
        direccion.setId(20L); direccion.setNivelDepartamento("DIRECCION"); direccion.setRutJefe(300);
        when(departamentos.obtenerDepartamento(10L)).thenReturn(subdireccion);
        when(departamentos.obtenerDepartamento(20L)).thenReturn(direccion);
        when(subrogancias.findFirstByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(any(), any(), any()))
                .thenReturn(Optional.empty());
        assertEquals(200, servicio.getRutFirmante(solicitud));
    }
}
