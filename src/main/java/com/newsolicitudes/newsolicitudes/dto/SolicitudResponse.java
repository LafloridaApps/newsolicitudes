package com.newsolicitudes.newsolicitudes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitudResponse {

    private Long id;
    private String nombreDepartamento;
    private String jefeDestino;
    private String mensaje;

    public SolicitudResponse() {
    }

    public SolicitudResponse(Long id, String nombreDepartamento) {
        this.id = id;
        this.nombreDepartamento = nombreDepartamento;
    }

    public SolicitudResponse(Long id, String nombreDepartamento, String mensaje) {
        this.id = id;
        this.nombreDepartamento = nombreDepartamento;
        this.mensaje = mensaje;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

}
