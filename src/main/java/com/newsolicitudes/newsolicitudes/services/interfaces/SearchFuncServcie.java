package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;

public interface SearchFuncServcie {

    FuncionarioResponse getDirectorActivo(Long idDepartamento, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud);

    FuncionarioResponse buscarSubroganteByRut(Integer rut, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud);

    List<FuncionarioResponse> buscarFuncionarioByNombre(String patterno, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud, int pageNmber, Long idDepto);

}
