package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.ExisteSolicitudResponseDto;
import com.newsolicitudes.newsolicitudes.dto.PageMiSolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDetalleDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.UpdateSolicitudRequest;

public interface SolicitudService {

    SolicitudResponse createSolicitud(SolicitudRequest request);

    ExisteSolicitudResponseDto existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo);

    PageMiSolicitudResponse getSolicitudesByRut(Integer rut, int page, int size);

    SolicitudDetalleDto getSolicitudDetalleById(Long idSolicitud);

    void updateSolicitud(Long idSolicitud, UpdateSolicitudRequest request);

}
