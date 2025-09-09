package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class DepartamentoSubrogadoDTO {
    private String nombreDepartamento;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    public DepartamentoSubrogadoDTO(String nombreDepartamento, LocalDate fechaDesde, LocalDate fechaHasta) {
        this.nombreDepartamento = nombreDepartamento;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }
}