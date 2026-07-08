package com.newsolicitudes.newsolicitudes.services.busqueda;

import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;

public interface BusquedaSolicitudService {

    PageSolicitudesResponse buscarSolicitudes(Long codDepto, String nombreSolicitante, String rutSolicitante,
            String fechaInicio, String fechaTermino, int pageNumber);

}
