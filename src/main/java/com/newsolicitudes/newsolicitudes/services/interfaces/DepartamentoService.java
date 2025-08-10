package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;

public interface DepartamentoService {

    DepartamentoResponse getDepartamentoDestino(Integer rutJefe, DepartamentoResponse departamento);

    DepartamentoResponse getDepartamentoById(Long id);

}
