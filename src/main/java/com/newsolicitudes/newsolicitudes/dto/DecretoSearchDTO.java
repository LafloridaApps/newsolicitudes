
package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class DecretoSearchDTO {

    private Long idDecreto;
    private LocalDate fechaDecreto;
    private Integer rutFuncionario;
    private Long idSolicitud;
    private String nombreFuncionario; // Se llenará externamente si es posible

    public DecretoSearchDTO(Long idDecreto, LocalDate fechaDecreto, Integer rutFuncionario, Long idSolicitud) {
        this.idDecreto = idDecreto;
        this.fechaDecreto = fechaDecreto;
        this.rutFuncionario = rutFuncionario;
        this.idSolicitud = idSolicitud;
    }

    // Getters y Setters
    public Long getIdDecreto() {
        return idDecreto;
    }

    public void setIdDecreto(Long idDecreto) {
        this.idDecreto = idDecreto;
    }

    public LocalDate getFechaDecreto() {
        return fechaDecreto;
    }

    public void setFechaDecreto(LocalDate fechaDecreto) {
        this.fechaDecreto = fechaDecreto;
    }

    public Integer getRutFuncionario() {
        return rutFuncionario;
    }

    public void setRutFuncionario(Integer rutFuncionario) {
        this.rutFuncionario = rutFuncionario;
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getNombreFuncionario() {
        return nombreFuncionario;
    }

    public void setNombreFuncionario(String nombreFuncionario) {
        this.nombreFuncionario = nombreFuncionario;
    }
}
