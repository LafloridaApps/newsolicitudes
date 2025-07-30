package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiNewFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioApiService;
import com.newsolicitudes.newsolicitudes.utlils.PersonaUtils;

@Service
public class FuncionarioApiServiceImpl implements FuncionarioApiService {

    private final ApiNewFuncionarioService apiNewFuncionarioService;

    public FuncionarioApiServiceImpl(ApiNewFuncionarioService apiNewFuncionarioService) {
        this.apiNewFuncionarioService = apiNewFuncionarioService;
    }

    @Override
    public FuncionarioResponse getFuncionarioInfo(Integer rut, String vRut) {

        if (rut == null || vRut.isEmpty()) {
            throw new FuncionarioException("Rut no puede tener carcteres invalidos o en blanco");
        }

        if (Boolean.FALSE.equals(PersonaUtils.validateRut(rut, vRut))) {
            throw new FuncionarioException("Rut no valido");
        }

        return apiNewFuncionarioService.obtenerDetalleColaborador(rut);
      
    }

}
