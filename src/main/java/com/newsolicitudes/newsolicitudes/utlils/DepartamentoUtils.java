package com.newsolicitudes.newsolicitudes.utlils;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;

public class DepartamentoUtils {

    private DepartamentoUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static NivelDepartamento getNivelDepartamento(DepartamentoResponse departamento) {
        return NivelDepartamento.valueOf(departamento.getNivelDepartamento());
    }

    public static TipoDerivacion tipoPorNivel(NivelDepartamento nivel, String tipoContrato) {
        if (nivel == NivelDepartamento.SUBDIRECCION && !permiteFirmaSubdireccion(tipoContrato)) {
            return TipoDerivacion.VISACION;
        }
        return tipoPorNivel(nivel);
    }

    private static boolean permiteFirmaSubdireccion(String tipoContrato) {
        if (tipoContrato == null) return false;
        return switch (tipoContrato.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "PLANTA", "CONTRATA", "SUPLENCIA" -> true;
            default -> false;
        };
    }

    public static TipoDerivacion tipoPorNivel(NivelDepartamento nivel) {
        return switch (nivel) {
            case DEPARTAMENTO, SECCION, OFICINA -> TipoDerivacion.VISACION;
            case ALCALDIA, DIRECCION, SUBDIRECCION, ADMINISTRACION -> TipoDerivacion.FIRMA;
            default -> TipoDerivacion.VISACION;
        };
    }

}
