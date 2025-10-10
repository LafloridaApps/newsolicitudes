package com.newsolicitudes.newsolicitudes.dto;


import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public class SolicitudInfoDTO {

    private Long idSolicitud;
    private Integer rutFuncionario;
    private String nombreFuncionario;
    private Solicitud.TipoSolicitud tipoSolicitud;
    private String urlPdf;


    public SolicitudInfoDTO(Long idSolicitud, Integer rutFuncionario, String nombreFuncionario, Solicitud.TipoSolicitud tipoSolicitud, String urlPdf) {
        this.idSolicitud = idSolicitud;
        this.rutFuncionario = rutFuncionario;
        this.nombreFuncionario = nombreFuncionario;
        this.tipoSolicitud = tipoSolicitud;
        this.urlPdf = urlPdf;
    }

    // Getters and Setters
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

    public Solicitud.TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(Solicitud.TipoSolicitud tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }
}
