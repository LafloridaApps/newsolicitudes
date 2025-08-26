package com.newsolicitudes.newsolicitudes.services.apiadministrativo;

import com.newsolicitudes.newsolicitudes.dto.ApiAdministrativoResponse;

public interface ApiAdmistrativoService {

    ApiAdministrativoResponse obtenerAdministrativos(Integer rut, Integer ident);

}
