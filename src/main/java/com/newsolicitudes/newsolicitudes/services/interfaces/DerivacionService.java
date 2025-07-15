package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;
import java.util.List;

public interface DerivacionService {

    Derivacion createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo, Departamento departamento, EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions;

    List<SolicitudDto> getDerivacionesByFuncionario(Integer rut);

  

}
