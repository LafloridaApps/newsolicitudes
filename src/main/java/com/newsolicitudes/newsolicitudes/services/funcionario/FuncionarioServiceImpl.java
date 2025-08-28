package com.newsolicitudes.newsolicitudes.services.funcionario;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final ApiExtFuncionarioService apiExtFuncionarioService;

    public FuncionarioServiceImpl(ApiExtFuncionarioService apiExtFuncionarioService) {
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    public FuncionarioResponseApi getFuncionarioByRut(Integer rut) {
        return apiExtFuncionarioService.obtenerDetalleColaborador(rut);
    }

}
