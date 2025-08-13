package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class SubroganciaInfo {

    private Long idDeptoSubrogado;
    private String nombreDeptoSubrogado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public SubroganciaInfo() {
    }

    public SubroganciaInfo(Long idDeptoSubrogado, String nombreDeptoSubrogado, LocalDate fechaInicio, LocalDate fechaFin) {
        this.idDeptoSubrogado = idDeptoSubrogado;
        this.nombreDeptoSubrogado = nombreDeptoSubrogado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getIdDeptoSubrogado() {
        return idDeptoSubrogado;
    }

    public void setIdDeptoSubrogado(Long idDeptoSubrogado) {
        this.idDeptoSubrogado = idDeptoSubrogado;
    }

    public String getNombreDeptoSubrogado() {
        return nombreDeptoSubrogado;
    }

    public void setNombreDeptoSubrogado(String nombreDeptoSubrogado) {
        this.nombreDeptoSubrogado = nombreDeptoSubrogado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
