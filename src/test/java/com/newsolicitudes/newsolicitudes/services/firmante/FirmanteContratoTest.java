package com.newsolicitudes.newsolicitudes.services.firmante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Optional;
import com.newsolicitudes.newsolicitudes.dto.*;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.*;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

class FirmanteContratoTest {
    @ParameterizedTest
    @ValueSource(strings = {"HONORARIOS", "PLANTA", "CONTRATA", "SUPLENCIA"})
    void seleccionaFirmanteSegunContratoDelSolicitante(String contrato) {
        var departamentos = mock(ApiDepartamentoService.class, withSettings().mockMaker("mock-maker-proxy"));
        var subrogancias = mock(SubroganciaRepository.class, withSettings().mockMaker("mock-maker-proxy"));
        var aprobaciones = mock(AprobacionRepository.class, withSettings().mockMaker("mock-maker-proxy"));
        var funcionarios = mock(FuncionarioService.class, withSettings().mockMaker("mock-maker-proxy"));
        var servicio = new FirmanteServiceImpl(departamentos, subrogancias, aprobaciones, funcionarios);
        var solicitud = new Solicitud();
        solicitud.setRut(100); solicitud.setIdDepto(10L);
        var funcionario = new FuncionarioResponseApi(); funcionario.setTipoContrato(contrato);
        when(funcionarios.getFuncionarioByRut(100)).thenReturn(funcionario);
        var subdireccion = new DepartamentoResponse();
        subdireccion.setId(10L); subdireccion.setNivelDepartamento("SUBDIRECCION");
        subdireccion.setRutJefe(200); subdireccion.setIdDeptoSuperior(20L);
        var direccion = new DepartamentoResponse();
        direccion.setId(20L); direccion.setNivelDepartamento("DIRECCION"); direccion.setRutJefe(300);
        when(departamentos.obtenerDepartamento(10L)).thenReturn(subdireccion);
        when(departamentos.obtenerDepartamento(20L)).thenReturn(direccion);
        when(subrogancias.findFirstByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(any(), any(), any()))
                .thenReturn(Optional.empty());
        assertEquals(contrato.equals("HONORARIOS") ? 300 : 200, servicio.getRutFirmante(solicitud));
    }
}
