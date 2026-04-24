package com.newsolicitudes.newsolicitudes.service.asistencia;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.AsistenciaDto;

import java.time.LocalDate;

public interface ApiAsistenciaService {

    List<AsistenciaDto> getAsitencia(Integer rut, Integer ident, LocalDate fechaInicio,
            LocalDate fechaFin);

}
