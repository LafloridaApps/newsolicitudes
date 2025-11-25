package com.newsolicitudes.newsolicitudes.services.derivacion;

import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;

public interface DerivacionService {

    //Crea la derivación inicial para una nueva solicitud.
    void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo, Long idDepto,
            EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions;

    // Obtiene una página de solicitudes basadas en las derivaciones de un departamento.
    PageSolicitudesResponse getDerivacionesByDeptoId(Long idDepto, int pageNumber, Boolean noLeidas);

    // Crea la siguiente derivación en la cadena, cuando un usuario visa o aprueba.
    void crearSiguienteDerivacion(Long idDerivacionAnterior, Integer rutUsuario);

}
