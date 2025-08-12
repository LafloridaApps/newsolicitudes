package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;

public interface DerivacionService {

    void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo, Long idDepto,
            EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions;

    PageSolicitudesResponse getDerivacionesByDeptoId(Long idDepto, int pageNumber);

   

}
