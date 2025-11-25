package com.newsolicitudes.newsolicitudes.services.calculodias;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.repositories.FeriadoRepository;

@Service
public class CalculadoraDiasService {

    private final FeriadoRepository feriadoRepository;

    public CalculadoraDiasService(FeriadoRepository feriadoRepository) {
        this.feriadoRepository = feriadoRepository;
    }

    public double calcularDias(SolicitudRequest request) {
        String tipo = request.getTipoSolicitud();
        if (tipo.equalsIgnoreCase("ADMINISTRATIVO")) {
            return calcularDiasAdministrativo(request);
        } else if (tipo.equalsIgnoreCase("FERIADO")) {
            return calcularDiasFeriado(request);
        }
        return 0;
    }

    private double calcularDiasAdministrativo(SolicitudRequest request) {
        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();

        if (inicio.equals(fin)) {
            return request.getJornadaInicio().equals(request.getJornadaTermino()) ? 0.5 : 1.0;
        }

        double diasHabiles = contarDiasHabiles(inicio, fin);

        if (request.getJornadaInicio().equalsIgnoreCase(request.getJornadaTermino())) {
            diasHabiles -= 0.5;
        }

        return diasHabiles;
    }

    private double calcularDiasFeriado(SolicitudRequest request) {
        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();
        return contarDiasHabiles(inicio, fin);
    }

    private double contarDiasHabiles(LocalDate inicio, LocalDate fin) {
        double dias = 0;
        LocalDate fecha = inicio;

        while (!fecha.isAfter(fin)) {
            if (esDiaHabil(fecha)) {
                dias++;
            }
            fecha = fecha.plusDays(1);
        }

        return dias;
    }

    private boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);
        boolean esFeriado = feriadoRepository.existsByFecha(fecha);

        return !esFinDeSemana && !esFeriado;
    }
}
