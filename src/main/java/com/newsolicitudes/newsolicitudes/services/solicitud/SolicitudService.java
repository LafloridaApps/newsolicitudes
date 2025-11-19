package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.time.LocalDate;

import java.util.Map;

import com.newsolicitudes.newsolicitudes.dto.PageMiSolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDetalleDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.UpdateSolicitudRequest;

public interface SolicitudService {

    SolicitudResponse createSolicitud(SolicitudRequest request);

    Map<String, Object> existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo);

    PageMiSolicitudResponse getSolicitudesByRut(Integer rut, int page, int size);

    SolicitudDetalleDto getSolicitudDetalleById(Long idSolicitud);

    void updateSolicitud(Long idSolicitud, UpdateSolicitudRequest request);

}
