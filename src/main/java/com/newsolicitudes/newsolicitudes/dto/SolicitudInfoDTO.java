package com.newsolicitudes.newsolicitudes.dto;

public class SolicitudInfoDTO {

    private Long idSolicitud;
    private Integer rutFuncionario;
    private String nombreFuncionario;

    public SolicitudInfoDTO(Long idSolicitud, Integer rutFuncionario, String nombreFuncionario) {
        this.idSolicitud = idSolicitud;
        this.rutFuncionario = rutFuncionario;
        this.nombreFuncionario = nombreFuncionario;
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
}
