package com.newsolicitudes.newsolicitudes.services.feriadoslegales;

import com.newsolicitudes.newsolicitudes.dto.ApiFeriadosResponse;
import com.newsolicitudes.newsolicitudes.dto.FeriadosLegalesDto;
import com.newsolicitudes.newsolicitudes.services.apiferiados.ApiFeriadoLegalServiceImpl;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FeriadosLegalesServiceImpl implements FeriadosLegalesService {

    private final ApiFeriadoLegalServiceImpl apiFeriadoLegalService;

    public FeriadosLegalesServiceImpl(ApiFeriadoLegalServiceImpl apiFeriadoLegalService) {
        this.apiFeriadoLegalService = apiFeriadoLegalService;
    }

    @Override
    public FeriadosLegalesDto obtenerFeriados(Integer rut, Integer ident) {

        ApiFeriadosResponse response = apiFeriadoLegalService.obtenerFeriadosByRut(rut, ident);

        if (response == null) {
            return new FeriadosLegalesDto();
        }

        List<FeriadosLegalesDto.DetalleFeriado> detalle = obtenerDetalle(response.getDetalle());

        return FeriadosLegalesDto.builder()
                .anio(response.getAnio() == null ? Year.now().getValue() : response.getAnio() )
                .diasAcumulados(response.getDiasAcumulados())
                .total(response.getTotal())
                .diasTomados(response.getDiasTomados())
                .diasPerdidos(response.getDiasPerdidos())
                .diasPendientes(response.getDiasPendientes())
                .diasCorresponden(response.getDiasCorresponden())
                .detalle(detalle)
                .build();

    }

    private List<FeriadosLegalesDto.DetalleFeriado> obtenerDetalle(List<ApiFeriadosResponse.DetalleFeriado> detalle) {

        if (detalle == null) {
            return java.util.Collections.emptyList();
        }

        return detalle.stream()
                .map(d -> new FeriadosLegalesDto.DetalleFeriado(
                        d.getNumero(),
                        d.getResolucion(),
                        d.getFechaResolucion(),
                        d.getFechaInicio(),
                        d.getFechaTermino(),
                        d.getPeriodo()))
                .toList();

    }

}
