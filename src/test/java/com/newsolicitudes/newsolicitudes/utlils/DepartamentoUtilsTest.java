package com.newsolicitudes.newsolicitudes.utlils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;

class DepartamentoUtilsTest {
    @ParameterizedTest
    @ValueSource(strings = {"PLANTA", "CONTRATA", "SUPLENCIA", " planta "})
    void subdireccionFirmaParaContratosPermitidos(String contrato) {
        assertEquals(TipoDerivacion.FIRMA,
                DepartamentoUtils.tipoPorNivel(NivelDepartamento.SUBDIRECCION, contrato));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"HONORARIOS", "CODIGO DEL TRABAJO", "OTRO"})
    void otrosContratosRequierenVisacionDeSubdireccionYFirmaDeDireccion(String contrato) {
        assertEquals(TipoDerivacion.VISACION,
                DepartamentoUtils.tipoPorNivel(NivelDepartamento.SUBDIRECCION, contrato));
        assertEquals(TipoDerivacion.FIRMA,
                DepartamentoUtils.tipoPorNivel(NivelDepartamento.DIRECCION, contrato));
        assertEquals(TipoDerivacion.VISACION,
                DepartamentoUtils.tipoPorNivel(NivelDepartamento.DEPARTAMENTO, contrato));
    }
}
