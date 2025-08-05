package com.newsolicitudes.newsolicitudes.dto;

public class SolicitudResponse {

    private Long id;
    private String nombreDepartamento;
    private String jefeDestino;

    public SolicitudResponse() {
    }

    public SolicitudResponse(Long id, String nombreDepartamento) {
        this.id = id;
        this.nombreDepartamento = nombreDepartamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    public String getJefeDestino() {
        return jefeDestino;
    }

    public void setJefeDestino(String jefeDestino) {
        this.jefeDestino = jefeDestino;
    }

}
