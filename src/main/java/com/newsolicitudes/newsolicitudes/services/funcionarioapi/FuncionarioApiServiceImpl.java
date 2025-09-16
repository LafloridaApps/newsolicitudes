package com.newsolicitudes.newsolicitudes.services.funcionarioapi;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

@Service
public class FuncionarioApiServiceImpl implements FuncionarioApiService {

    private final ApiExtFuncionarioService apiExtFuncionarioService;

    public FuncionarioApiServiceImpl(ApiExtFuncionarioService apiExtFuncionarioService) {
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    public FuncionarioResponseApi getFuncionarioInfo(Integer rut) {

        if (rut == null ) {
            throw new FuncionarioException("Rut no puede tener carcteres invalidos o en blanco");
        }

      

        return apiExtFuncionarioService.obtenerDetalleColaborador(rut);

    }

}
