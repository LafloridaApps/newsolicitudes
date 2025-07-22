package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioPageResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;

public interface SearchFunc {

    FuncionarioPageResponse searchFuncionario(String nombre, Long idDepartamento, LocalDate fechaInicio, LocalDate fechaTermino);

    FuncionarioResponse getDirectorActivo(Long idDepartamento, LocalDate fechaInicioSolicitud, 
    LocalDate fechaFinSolicitud);

}
