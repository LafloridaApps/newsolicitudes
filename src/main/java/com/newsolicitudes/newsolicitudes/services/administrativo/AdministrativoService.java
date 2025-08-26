package com.newsolicitudes.newsolicitudes.services.administrativo;

import com.newsolicitudes.newsolicitudes.dto.AdministrativoDto;

public interface AdministrativoService {

    AdministrativoDto getAdministrativoByRutAndIdent(Integer rut, Integer ident);

}
