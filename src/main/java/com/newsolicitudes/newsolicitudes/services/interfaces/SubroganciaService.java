package com.newsolicitudes.newsolicitudes.services.interfaces;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;

public interface SubroganciaService {

    void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin, Long idDepto);


}
