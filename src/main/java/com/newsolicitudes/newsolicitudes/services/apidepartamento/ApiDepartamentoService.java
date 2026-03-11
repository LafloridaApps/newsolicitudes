package com.newsolicitudes.newsolicitudes.services.apidepartamento;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.RecordDepartamentoRequest;

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

    Object updateJefeDepartamento(Long idDepto, Integer rut);

    Object updateNombreDepartamento(Long idDepto, String nombre);

    Object updateCodigoExternoDepartamento(Long idDepto, String codigoExterno);

    Object delteCodigoExternoByIdDepto(Long idDepto);

    Object agregarDepartamento(RecordDepartamentoRequest request);

}
