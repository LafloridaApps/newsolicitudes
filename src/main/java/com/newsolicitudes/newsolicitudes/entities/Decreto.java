package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import jakarta.persistence.CascadeType;

@Entity
public class Decreto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "decreto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecretoSolicitud> decretoSolicitudes;

    private Integer realizadoPor;

    private LocalDateTime fechaHoraTransaccion;

    private LocalDate fechaDecreto;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<DecretoSolicitud> getDecretoSolicitudes() {
        return decretoSolicitudes;
    }

    public void setDecretoSolicitudes(List<DecretoSolicitud> decretoSolicitudes) {
        this.decretoSolicitudes = decretoSolicitudes;
    }

    public Integer getRealizadoPor() {
        return realizadoPor;
    }

    public void setRealizadoPor(Integer realizadoPor) {
        this.realizadoPor = realizadoPor;
    }

    public LocalDateTime getFechaHoraTransaccion() {
        return fechaHoraTransaccion;
    }

    public void setFechaHoraTransaccion(LocalDateTime fechaHoraTransaccion) {
        this.fechaHoraTransaccion = fechaHoraTransaccion;
    }

    public LocalDate getFechaDecreto() {
        return fechaDecreto;
    }

    public void setFechaDecreto(LocalDate fechaDecreto) {
        this.fechaDecreto = fechaDecreto;
    }
}
