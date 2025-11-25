package com.newsolicitudes.newsolicitudes.dto;

public class ExisteSolicitudResponseDto {

    private boolean existe;
    private String estado;
    private String fechaInicio;
    private String fechaTermino;
    private String jornadaInicio;
    private String jornadaTermino;

    public ExisteSolicitudResponseDto() {
    }

    public ExisteSolicitudResponseDto(boolean existe, String estado, String fechaInicio, String fechaTermino, String jornadaInicio, String jornadaTermino) {
        this.existe = existe;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaTermino = fechaTermino;
        this.jornadaInicio = jornadaInicio;
        this.jornadaTermino = jornadaTermino;
    }

    public boolean isExiste() {
        return existe;
    }

    public void setExiste(boolean existe) {
        this.existe = existe;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
}
