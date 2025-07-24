package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoRequest;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.JefeFunc;
import com.newsolicitudes.newsolicitudes.entities.Departamento;

public interface DepartamentoService {

    DepartamentoResponse createDepartamento(DepartamentoRequest request);

    List<DepartamentoList> getDepartamentosList();

    void updateDepartamento(Long idDepartamento, DepartamentoRequest request);

    void updateJefeDeparatmento(Long idDepto, Integer rut);

    JefeFunc isJefeDepartamento(Long codDepto, Integer rut);

    Departamento getDepartamentoDestino(Departamento departamento, Integer rutSolicicante);
}
