package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class ProximaAusenciaDTO {
    private String nombreFuncionario;
    private LocalDate fechaAusencia;

    public ProximaAusenciaDTO(String nombreFuncionario, LocalDate fechaAusencia) {
        this.nombreFuncionario = nombreFuncionario;
        this.fechaAusencia = fechaAusencia;
    }

    public String getNombreFuncionario() {
        return nombreFuncionario;
    }

    public void setNombreFuncionario(String nombreFuncionario) {
        this.nombreFuncionario = nombreFuncionario;
    }

    public LocalDate getFechaAusencia() {
        return fechaAusencia;
    }

    public void setFechaAusencia(LocalDate fechaAusencia) {
        this.fechaAusencia = fechaAusencia;
    }
}