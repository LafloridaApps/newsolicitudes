package com.newsolicitudes.newsolicitudes.services.solicitud;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import com.newsolicitudes.newsolicitudes.dto.*;
import com.newsolicitudes.newsolicitudes.exceptions.SolicitudException;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

class SolicitudDepartamentoTest {
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"SIN_ASOCIACION", "SERVICIO_NO_DISPONIBLE", "NO_VERIFICADO", "SIN_CODIGO_ORIGEN"})
    void noGuardaNiDerivaConDepartamentoSinValidarAunqueConserveUnoAnterior(String estado) {
        var funcionarios = mock(FuncionarioService.class);
        var solicitudes = mock(SolicitudRepository.class);
        var funcionario = new FuncionarioResponseApi();
        funcionario.setRut(123); funcionario.setCodDepto(7L);
        if (estado != null) funcionario.setConfiguracionDepartamento(new ConfiguracionDepartamento(estado, "17100801", "Oficina"));
        when(funcionarios.getFuncionarioByRut(123)).thenReturn(funcionario);
        var service = new SolicitudServiceImpl(null, solicitudes, null, null, funcionarios,
                null, null, null, null, null, null, null, null);
        var request = new SolicitudRequest(); request.setRut(123);
        var error = assertThrows(SolicitudException.class, () -> service.createSolicitud(request));
        assertTrue(error.getMessage().contains("asociación"));
        verifyNoInteractions(solicitudes);
    }
}
