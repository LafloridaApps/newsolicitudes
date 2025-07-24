package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface SubroganciaService {

    void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin, Long idDepto);

    boolean estaSubrogandoNivelSuperior(Departamento departamento, Solicitud solicitud);

}
