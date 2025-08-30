package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class DecretoDto {
    private Long id;
    private LocalDate fechaDecreto;

    public DecretoDto() {
    }

    public DecretoDto(Long id, LocalDate fechaDecreto) {
        this.id = id;
        this.fechaDecreto = fechaDecreto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaDecreto() {
        return fechaDecreto;
    }

    public void setFechaDecreto(LocalDate fechaDecreto) {
        this.fechaDecreto = fechaDecreto;
    }
}