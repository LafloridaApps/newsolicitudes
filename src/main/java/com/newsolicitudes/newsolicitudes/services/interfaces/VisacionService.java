package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface VisacionService {

    void visarSolicitud(Solicitud solicitud, Funcionario funcionario);

}
