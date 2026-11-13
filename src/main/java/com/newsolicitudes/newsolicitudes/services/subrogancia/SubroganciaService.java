package com.newsolicitudes.newsolicitudes.services.subrogancia;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;

public interface SubroganciaService {

    Long createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin, Long idDepto);

    Long createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin,
            Long idDepto, com.newsolicitudes.newsolicitudes.services.ceropapel.SubroganciaCeroPapelEvent.Origen origen);

    void borrarSubrogancia(Long idSubrogancia);

}
