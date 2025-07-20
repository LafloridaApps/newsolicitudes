package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class SubroganciaRequest {

    private Integer rutSubrogante;
    private Integer rutJefe;
    private Long codDEpto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Integer getRutSubrogante() {
        return rutSubrogante;
    }

    public void setRutSubrogante(Integer rutSubrogante) {
        this.rutSubrogante = rutSubrogante;
    }

    public Integer getRutJefe() {
        return rutJefe;
    }

    public void setRutJefe(Integer rutJefe) {
        this.rutJefe = rutJefe;
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

    public Long getCodDEpto() {
        return codDEpto;
    }

    public void setCodDEpto(Long codDEpto) {
        this.codDEpto = codDEpto;
    }

}
