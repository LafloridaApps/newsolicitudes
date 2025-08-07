package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.CargoFunc;

public interface ApiDepartamentoService {

    DepartamentoResponse obtenerDepartamento(Long idDepto);

    CargoFunc obtenerJefeFunc(Long idDepto, Integer rut);

}
