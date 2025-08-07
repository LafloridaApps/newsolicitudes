package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;

public interface SearchFuncServcie {

    FuncionarioResponse getDirectorActivo(Long idDepartamento, LocalDate fechaInicioSolicitud, 
    LocalDate fechaFinSolicitud);

}
