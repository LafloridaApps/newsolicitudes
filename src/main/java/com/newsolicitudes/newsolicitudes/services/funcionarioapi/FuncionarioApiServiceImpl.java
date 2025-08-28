package com.newsolicitudes.newsolicitudes.services.funcionarioapi;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.utlils.PersonaUtils;

@Service
public class FuncionarioApiServiceImpl implements FuncionarioApiService {

    private final ApiExtFuncionarioService apiExtFuncionarioService;

    public FuncionarioApiServiceImpl(ApiExtFuncionarioService apiExtFuncionarioService) {
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    public FuncionarioResponseApi getFuncionarioInfo(Integer rut, String vRut) {

        if (rut == null || vRut.isEmpty()) {
            throw new FuncionarioException("Rut no puede tener carcteres invalidos o en blanco");
        }

        if (Boolean.FALSE.equals(PersonaUtils.validateRut(rut, vRut))) {
            throw new FuncionarioException("Rut no valido");
        }

        return apiExtFuncionarioService.obtenerDetalleColaborador(rut);

    }

}
