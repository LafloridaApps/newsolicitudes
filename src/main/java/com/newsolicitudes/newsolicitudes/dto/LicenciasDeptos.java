package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public record LicenciasDeptos(
        LocalDate fechaInicio,
        LocalDate fechaTermino,
        String depto,
        int ident,
        Integer rut) {

}
