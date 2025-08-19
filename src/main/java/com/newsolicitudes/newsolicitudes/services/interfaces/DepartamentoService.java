package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;

public interface DepartamentoService {

    DepartamentoResponse getDepartamentoDestino(Integer rutJefe, DepartamentoResponse departamento, LocalDate fechaInicio, LocalDate fechaFin);

    DepartamentoResponse getDepartamentoById(Long id);

}
