package com.newsolicitudes.newsolicitudes.dto;

public record SearchnNombreDto(String nombreCompleto, Integer rut, Character vrut, Long codDepto, String departamento) {
    public SearchnNombreDto(String nombreCompleto, Integer rut, Character vrut) {
        this(nombreCompleto, rut, vrut, null, null);
    }
}
