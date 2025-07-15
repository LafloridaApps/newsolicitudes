package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Visacion;
import com.newsolicitudes.newsolicitudes.repositories.VisacionRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.VisacionService;

@Service
public class VisacionServiceImpl implements VisacionService {

    private final VisacionRepository visacionRepository;

    public VisacionServiceImpl(VisacionRepository visacionRepository) {
        this.visacionRepository = visacionRepository;
    }

    @Override
    public void visarSolicitud(Solicitud solicitud, Funcionario funcionario) {

        Visacion visacion = new Visacion();
        visacion.setSolicitud(solicitud);
        visacion.setFuncionario(funcionario);
        visacion.setFechaVisacion(LocalDate.now());
        visacionRepository.save(visacion);

    }

}
