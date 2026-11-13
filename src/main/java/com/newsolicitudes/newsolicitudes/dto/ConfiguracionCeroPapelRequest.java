package com.newsolicitudes.newsolicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConfiguracionCeroPapelRequest(
        @NotNull Boolean habilitada,
        @NotBlank @Size(max = 2048) String url,
        @NotBlank @Size(max = 100) String usuario,
        @Size(max = 1900) String clave,
        @NotNull Long version) {
    @Override
    public String toString() {
        return "ConfiguracionCeroPapelRequest[habilitada=" + habilitada + ", clave=OCULTA]";
    }
}
