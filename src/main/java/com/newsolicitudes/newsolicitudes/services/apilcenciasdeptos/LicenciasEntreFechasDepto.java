package com.newsolicitudes.newsolicitudes.services.apilcenciasdeptos;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.LicenciasDeptos;

public interface LicenciasEntreFechasDepto {

    List<LicenciasDeptos> obtenerLicencias(List<String> deptos, LocalDate fechaInicio, LocalDate fechaTermino);

}
