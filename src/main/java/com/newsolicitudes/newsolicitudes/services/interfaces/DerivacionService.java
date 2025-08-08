package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;

import java.util.List;

public interface DerivacionService {

    void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo, Long idDepto,
            EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions;

    List<SolicitudDto> getDerivacionesByDeptoId(Long idDepto);

}
