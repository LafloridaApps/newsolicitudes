package com.newsolicitudes.newsolicitudes.services.departamento;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;

public interface DepartamentoService {

    DepartamentoResponse getDepartamentoDestino(Integer rutJefe, DepartamentoResponse departamento,
            LocalDate fechaInicio, LocalDate fechaFin);

    DepartamentoResponse getDepartamentoById(Long id);

    List<DepartamentoList> getDepartamentos();

}
