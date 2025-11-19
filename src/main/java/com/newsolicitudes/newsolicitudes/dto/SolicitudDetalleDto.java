package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class SolicitudDetalleDto {

    private Long idSolicitud;
    private Integer rutFuncionario;
    private String nombreFuncionario;
    private String tipoSolicitud;
    private String fechaCreacion;
    private String nombreDepartamento;
    private String fechaInicio;
    private String fechaTermino;
    private String estadoSolicitud;
    private String urlPdf;
    private double cantidadDias;

    private List<DerivacionDto> derivaciones;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getRutFuncionario() {
        return rutFuncionario;
    }

    public void setRutFuncionario(Integer rutFuncionario) {
        this.rutFuncionario = rutFuncionario;
    }

    public String getNombreFuncionario() {
        return nombreFuncionario;
    }

    public void setNombreFuncionario(String nombreFuncionario) {
        this.nombreFuncionario = nombreFuncionario;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(String fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }

    public List<DerivacionDto> getDerivaciones() {
        return derivaciones;
    }

    public void setDerivaciones(List<DerivacionDto> derivaciones) {
        this.derivaciones = derivaciones;
    }

    public double getCantidadDias() {
        return cantidadDias;
    }

    public void setCantidadDias(double cantidadDias) {
        this.cantidadDias = cantidadDias;
    }

    

}
