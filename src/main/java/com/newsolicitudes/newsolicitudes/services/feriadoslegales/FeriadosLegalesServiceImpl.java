package com.newsolicitudes.newsolicitudes.services.feriadoslegales;

import com.newsolicitudes.newsolicitudes.dto.ApiFeriadosResponse;
import com.newsolicitudes.newsolicitudes.dto.FeriadosLegalesDto;
import com.newsolicitudes.newsolicitudes.entities.Feriado;
import com.newsolicitudes.newsolicitudes.services.apiferiados.ApiFeriadoLegalServiceImpl;
import com.newsolicitudes.newsolicitudes.services.feriado.FeriadoService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FeriadosLegalesServiceImpl implements FeriadosLegalesService {

    private final ApiFeriadoLegalServiceImpl apiFeriadoLegalService;
    private final FeriadoService feriadoService;

    public FeriadosLegalesServiceImpl(ApiFeriadoLegalServiceImpl apiFeriadoLegalService, FeriadoService feriadoService) {
        this.apiFeriadoLegalService = apiFeriadoLegalService;
        this.feriadoService = feriadoService;
    }

    @Override
    public FeriadosLegalesDto obtenerFeriados(Integer rut, Integer ident) {

        ApiFeriadosResponse response = apiFeriadoLegalService.obtenerFeriadosByRut(rut, ident);

        if (response == null) {
            return new FeriadosLegalesDto();
        }

        List<FeriadosLegalesDto.DetalleFeriado> detalle = obtenerDetalle(response.getDetalle());

        return FeriadosLegalesDto.builder()
                .anio(response.getAnio() == null ?  FechaUtils.fechaActual().getYear() : response.getAnio() )
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
                        calcularPeriodo(d.getFechaInicio(), d.getFechaTermino())))
                .toList();

    }
    


   private double calcularPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {

    int cantidadDias = 0;

    List<Feriado> feriados = feriadoService.getAll();

    LocalDate fecha = fechaInicio;

    while (!fecha.isAfter(fechaFin)) {

        boolean esFinDeSemana =
                fecha.getDayOfWeek().equals(DayOfWeek.SATURDAY) ||
                fecha.getDayOfWeek().equals(DayOfWeek.SUNDAY);

        boolean esFeriado = false;

        for (Feriado feriado : feriados) {
            if (feriado.getFecha().equals(fecha)) {
                esFeriado = true;
                break;
            }
        }

        if (!esFinDeSemana && !esFeriado) {
            cantidadDias++;
        }

        fecha = fecha.plusDays(1);
    }

    return cantidadDias;
}
}
