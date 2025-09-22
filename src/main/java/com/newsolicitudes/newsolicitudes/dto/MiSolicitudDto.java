package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class MiSolicitudDto {

    private Long id;
    private String tipoSolicitud;
    private String fechaSolicitud;
    private String estadoSolicitud;
    private String fechaInicio;
    private String fechaFin;
    private double cantidadDias;
    private String urlPdf;
    private List<Trazabilidad> trazabilidad;

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
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

    public double getCantidadDias() {
        return cantidadDias;
    }

    public void setCantidadDias(double cantidadDias) {
        this.cantidadDias = cantidadDias;
    }

    public List<Trazabilidad> getTrazabilidad() {
        return trazabilidad;
    }

    public void setTrazabilidad(List<Trazabilidad> trazabilidad) {
        this.trazabilidad = trazabilidad;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }

}
