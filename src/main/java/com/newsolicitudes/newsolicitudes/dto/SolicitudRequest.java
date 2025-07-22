package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class SolicitudRequest {

    private Integer rut;
    private LocalDate fechaSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long depto;
    private String tipoSolicitud;
    private String jornadaInicio;
    private String jornadaTermino;
    private SubroganciaRequest subrogancia;

    public String getJornadaInicio() {
        return jornadaInicio;
    }

    public void setJornadaInicio(String jornadaInicio) {
        this.jornadaInicio = jornadaInicio;
    }

    public String getJornadaTermino() {
        return jornadaTermino;
    }

    public void setJornadaTermino(String jornadaTermino) {
        this.jornadaTermino = jornadaTermino;
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
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

    public Long getDepto() {
        return depto;
    }

    public void setDepto(Long depto) {
        this.depto = depto;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public SubroganciaRequest getSubrogancia() {
        return subrogancia;
    }

    public void setSubrogancia(SubroganciaRequest subrogancia) {
        this.subrogancia = subrogancia;
    }

}
