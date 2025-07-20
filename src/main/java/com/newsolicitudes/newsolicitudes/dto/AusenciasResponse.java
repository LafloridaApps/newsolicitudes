package com.newsolicitudes.newsolicitudes.dto;

public class AusenciasResponse {

    private Integer rut;
    private String fechaInicio;
    private String fechaTermino;
    private String tipoAusencia;
    private Integer ident;

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
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

    public String getTipoAusencia() {
        return tipoAusencia;
    }

    public void setTipoAusencia(String tipoAusencia) {
        this.tipoAusencia = tipoAusencia;
    }

    public Integer getIdent() {
        return ident;
    }

    public void setIdent(Integer ident) {
        this.ident = ident;
    }

}
