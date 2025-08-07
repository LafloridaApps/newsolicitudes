package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioApiService;
import com.newsolicitudes.newsolicitudes.utlils.PersonaUtils;

@Service
public class FuncionarioApiServiceImpl implements FuncionarioApiService {

    private final ApiFuncionarioService apiFuncionarioService;

    public FuncionarioApiServiceImpl(ApiFuncionarioService apiNewFuncionarioService) {
        this.apiFuncionarioService = apiNewFuncionarioService;
    }

    @Override
    public FuncionarioResponse getFuncionarioInfo(Integer rut, String vRut) {

        if (rut == null || vRut.isEmpty()) {
            throw new FuncionarioException("Rut no puede tener carcteres invalidos o en blanco");
        }

        if (Boolean.FALSE.equals(PersonaUtils.validateRut(rut, vRut))) {
            throw new FuncionarioException("Rut no valido");
        }

        return apiFuncionarioService.obtenerDetalleColaborador(rut);
      
    }

}
