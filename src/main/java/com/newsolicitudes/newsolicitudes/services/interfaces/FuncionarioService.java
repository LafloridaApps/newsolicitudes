package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;

public interface FuncionarioService {

    Funcionario getOrCreateFuncionario(ApiFuncionarioResponse request);

    ApiFuncionarioResponse getFuncionarioInfo(Integer rut);

    Funcionario getFuncionarioByRut(Integer rut);


}
