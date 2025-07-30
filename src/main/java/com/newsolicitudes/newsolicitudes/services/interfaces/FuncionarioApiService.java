package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;

public interface FuncionarioApiService {

    FuncionarioResponse getFuncionarioInfo(Integer rut, String vRut);

}
