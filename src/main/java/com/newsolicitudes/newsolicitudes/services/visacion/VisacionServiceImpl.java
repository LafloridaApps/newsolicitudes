package com.newsolicitudes.newsolicitudes.services.visacion;


import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Visacion;
import com.newsolicitudes.newsolicitudes.repositories.VisacionRepository;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

@Service
public class VisacionServiceImpl implements VisacionService {

    private final VisacionRepository visacionRepository;

    public VisacionServiceImpl(VisacionRepository visacionRepository) {
        this.visacionRepository = visacionRepository;
    }

    @Override
    public void visarSolicitud(Solicitud solicitud, Integer rut) {

        Visacion visacion = new Visacion();
        visacion.setSolicitud(solicitud);
        visacion.setRut(rut);
        visacion.setFechaVisacion(FechaUtils.fechaActual());
        visacionRepository.save(visacion);

    }

}
