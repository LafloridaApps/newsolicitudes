package com.newsolicitudes.newsolicitudes.dto;

public class DerivacionDto {

    private Long id;

    private String fechaDerivacion;
    private String nombreDepartamento;
    private String jefeDestino;
    private String tipoMovimiento;
    private String estadoDerivacion;
        private boolean recepcionada;

        

    public String getFechaDerivacion() {
        return fechaDerivacion;
    }

    public void setFechaDerivacion(String fechaDerivacion) {
        this.fechaDerivacion = fechaDerivacion;
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

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstadoDerivacion() {
        return estadoDerivacion;
    }

    public void setEstadoDerivacion(String estadoDerivacion) {
        this.estadoDerivacion = estadoDerivacion;
    }

    public boolean isRecepcionada() {
        return recepcionada;
    }

    public void setRecepcionada(boolean recepcionada) {
        this.recepcionada = recepcionada;
    }

}
