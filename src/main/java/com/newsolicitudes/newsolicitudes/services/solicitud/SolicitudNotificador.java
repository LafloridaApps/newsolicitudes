package com.newsolicitudes.newsolicitudes.services.solicitud;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface SolicitudNotificador {

    void enviarNotificacionNuevaSolicitud(Solicitud solicitud, DepartamentoResponse departamentoDestino, FuncionarioResponseApi funcionario, String nombreDepartamentoActual);

}