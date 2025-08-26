package com.newsolicitudes.newsolicitudes.services.funcionario;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiFuncionarioService;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final ApiFuncionarioService apiFuncionarioService;

    public FuncionarioServiceImpl(ApiFuncionarioService apiFuncionarioService) {
        this.apiFuncionarioService = apiFuncionarioService;
    }

    @Override
    public FuncionarioResponse getFuncionarioByRut(Integer rut) {
        return apiFuncionarioService.obtenerDetalleColaborador(rut);
    }

}
