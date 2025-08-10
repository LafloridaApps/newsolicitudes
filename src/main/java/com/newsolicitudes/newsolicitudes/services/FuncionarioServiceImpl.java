package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;

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
