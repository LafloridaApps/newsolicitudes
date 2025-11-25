package com.newsolicitudes.newsolicitudes.services.trazabilidad;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.Trazabilidad;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface TrazabilidadService {
    List<Trazabilidad> construirTrazabilidad(Solicitud solicitud);
}
