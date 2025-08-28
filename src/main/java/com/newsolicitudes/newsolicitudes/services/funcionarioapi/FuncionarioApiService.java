package com.newsolicitudes.newsolicitudes.services.funcionarioapi;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;

public interface FuncionarioApiService {

 FuncionarioResponseApi getFuncionarioInfo(Integer rut, String vRut);

}
