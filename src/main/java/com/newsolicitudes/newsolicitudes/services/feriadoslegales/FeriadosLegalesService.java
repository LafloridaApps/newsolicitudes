package com.newsolicitudes.newsolicitudes.services.feriadoslegales;

import com.newsolicitudes.newsolicitudes.dto.FeriadosLegalesDto;

public interface FeriadosLegalesService {

    FeriadosLegalesDto obtenerFeriados(Integer rut, Integer ident);

}
