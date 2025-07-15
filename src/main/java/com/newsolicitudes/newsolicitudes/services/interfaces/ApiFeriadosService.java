package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.ApiFeriadosResponse;

public interface ApiFeriadosService {

    ApiFeriadosResponse obtenerFeriadosByRut(Integer rut, Integer ident);

}
