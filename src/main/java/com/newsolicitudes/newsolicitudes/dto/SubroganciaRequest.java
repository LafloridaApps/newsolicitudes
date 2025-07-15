package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class SubroganciaRequest {

    private Long subroganteId;
    private Long jefeDepartamentoId;
    private Long departamentoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Long getSubroganteId() {
        return subroganteId;
    }

    public void setSubroganteId(Long subroganteId) {
        this.subroganteId = subroganteId;
    }

    public Long getJefeDepartamentoId() {
        return jefeDepartamentoId;
    }

    public void setJefeDepartamentoId(Long jefeDepartamentoId) {
        this.jefeDepartamentoId = jefeDepartamentoId;
    }

    public Long getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Long departamentoId) {
        this.departamentoId = departamentoId;
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
