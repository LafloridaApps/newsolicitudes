package com.newsolicitudes.newsolicitudes.services.derivacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;

class DerivacionContratoTest {
    @ParameterizedTest
    @ValueSource(strings = {"HONORARIOS", "PLANTA", "CONTRATA", "SUPLENCIA"})
    void determinaTipoDeSiguienteDerivacionSegunContrato(String contrato) {
        var subrogancias = mock(SubroganciaRepository.class, withSettings().mockMaker("mock-maker-proxy"));
        var servicio = new DerivacionServiceImpl(null, null, null, subrogancias, null, null, null, null, null, null, null);
        var hoy = LocalDate.of(2026, 11, 4);
        when(subrogancias.findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(200, hoy, hoy))
                .thenReturn(List.of());
        when(subrogancias.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(200, hoy, hoy))
                .thenReturn(List.of());
        var destino = new DepartamentoResponse();
        destino.setNivelDepartamento("SUBDIRECCION"); destino.setRutJefe(200);
        assertEquals(contrato.equals("HONORARIOS") ? TipoDerivacion.VISACION : TipoDerivacion.FIRMA,
                ReflectionTestUtils.invokeMethod(servicio, "determinaTipoDerivacionFinal", destino, hoy, contrato));
        destino.setNivelDepartamento("DIRECCION");
        assertEquals(TipoDerivacion.FIRMA,
                ReflectionTestUtils.invokeMethod(servicio, "determinaTipoDerivacionFinal", destino, hoy, contrato));
    }
}
