package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;

public interface FuncionarioService {

    Funcionario getOrCreateFuncionario(ApiFuncionarioResponse request);

    FuncionarioResponse getFuncionarioInfo(Integer rut, String vRut);



}
