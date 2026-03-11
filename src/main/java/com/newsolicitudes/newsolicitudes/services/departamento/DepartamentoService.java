package com.newsolicitudes.newsolicitudes.services.departamento;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.RecordDepartamentoRequest;

public interface DepartamentoService {

    DepartamentoResponse getDepartamentoDestino(Integer rutJefe, DepartamentoResponse departamento,
            LocalDate fechaInicio, LocalDate fechaFin);

    DepartamentoResponse getDepartamentoById(Long id);

    List<DepartamentoList> getDepartamentos();

    void updateJefeDepartamento(Long idDepto, Integer rutJefe);

    void updateNombreDepartamento(Long idDepto, String rut);

    void updateCodigoExterno(Long idDepto, String codigoExterno);

    void delteCodigoExternoByIdDepto(Long idDepto);

    void agregarDepartamento(RecordDepartamentoRequest request);

}
