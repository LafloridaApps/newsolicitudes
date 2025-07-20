package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;

public interface SolicitudService {

    SolicitudResponse createSolicitud(SolicitudRequest request);

    boolean existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo);

}
