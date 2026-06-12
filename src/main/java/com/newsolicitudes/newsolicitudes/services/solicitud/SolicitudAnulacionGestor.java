package com.newsolicitudes.newsolicitudes.services.solicitud;

import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface SolicitudAnulacionGestor {
    String anular(Solicitud solicitud, String motivo);

    String resolverAnulacion(Long idSolicitud, Integer rutAprobador);

    String anularDirecto(Solicitud solicitud, String motivo, Integer rutAprobador);
}