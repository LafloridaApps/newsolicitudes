package com.newsolicitudes.newsolicitudes.services.searchfunc;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;

public interface SearchFuncServcie {

    FuncionarioResponseApi getDirectorActivo(Long idDepartamento, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud);

    FuncionarioResponseApi buscarSubroganteByRut(Integer rut, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud);

    List<FuncionarioResponseApi> buscarFuncionarioByNombre(String patterno, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud, int pageNmber, Long idDepto);

}
