package com.newsolicitudes.newsolicitudes.utlils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;

class DepartamentoUtilsTest {
    @ParameterizedTest
    @EnumSource(value = NivelDepartamento.class, names = {"SUBDIRECCION", "DIRECCION", "ALCALDIA", "ADMINISTRACION"})
    void unidadesFirmantesSinDistincionDeContrato(NivelDepartamento nivel) {
        assertEquals(TipoDerivacion.FIRMA, DepartamentoUtils.tipoPorNivel(nivel));
    }

    @ParameterizedTest
    @EnumSource(value = NivelDepartamento.class, names = {"DEPARTAMENTO", "SECCION", "OFICINA"})
    void unidadesVisadoras(NivelDepartamento nivel) {
        assertEquals(TipoDerivacion.VISACION, DepartamentoUtils.tipoPorNivel(nivel));
    }
}
