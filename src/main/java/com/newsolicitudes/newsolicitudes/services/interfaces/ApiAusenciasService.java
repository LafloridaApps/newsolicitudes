package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.AusenciasResponse;

public interface ApiAusenciasService {

    List<AusenciasResponse> getAusenciasByRutAndFechas(Integer rut, Integer ident, LocalDate fechaInicio,
            LocalDate fechaFin);

}
