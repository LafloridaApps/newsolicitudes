package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.ApiAdministrativoResponse;

public interface ApiAdmistrativoService {

    ApiAdministrativoResponse obtenerAdministrativos(Integer rut, Integer ident);

}
