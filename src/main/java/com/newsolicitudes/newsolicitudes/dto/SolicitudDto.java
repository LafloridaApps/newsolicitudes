package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class SolicitudDto {

    private Long id;
    private String solicitante;
    private String fechaSolicitud;
    private String fechaInicio;
    private String fechaFin;
    private String jornadaInicio;
    private String jornadaFin;
    private String tipoSolicitud;
    private String departamentoOrigen;
    private String estadoSolicitud;
    private double cantidadDias;

    private List<DerivacionDto> derivaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getJornadaInicio() {
        return jornadaInicio;
    }

    public void setJornadaInicio(String jornadaInicio) {
        this.jornadaInicio = jornadaInicio;
    }

    public String getJornadaFin() {
        return jornadaFin;
    }

    public void setJornadaFin(String jornadaFin) {
        this.jornadaFin = jornadaFin;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getDepartamentoOrigen() {
        return departamentoOrigen;
    }

    public void setDepartamentoOrigen(String departamentoOrigen) {
        this.departamentoOrigen = departamentoOrigen;
    }

    public double getCantidadDias() {
        return cantidadDias;
    }

    public void setCantidadDias(double cantidadDias) {
        this.cantidadDias = cantidadDias;
    }

    public List<DerivacionDto> getDerivaciones() {
        return derivaciones;
    }

    public void setDerivaciones(List<DerivacionDto> derivaciones) {
        this.derivaciones = derivaciones;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

}
