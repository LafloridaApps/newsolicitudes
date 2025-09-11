package com.newsolicitudes.newsolicitudes.services.apidepartamento;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.CargoFunc;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoJerarquiaDTO;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;

public interface ApiDepartamentoService {

    DepartamentoResponse obtenerDepartamento(Long idDepto);

    CargoFunc obtenerJefeFunc(Long idDepto, Integer rut);

    List<DepartamentoResponse> obtenerFamiliaDepto(Long idDepto);

    List<DepartamentoList> getDepartamentosList();

     DepartamentoJerarquiaDTO getJerarquiaPorId(Long id);

}
