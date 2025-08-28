package com.newsolicitudes.newsolicitudes.services.apiferiados;

import com.newsolicitudes.newsolicitudes.dto.ApiFeriadosResponse;

public interface ApiFeriadoLegalService {

    ApiFeriadosResponse obtenerFeriadosByRut(Integer rut, Integer ident);

}
