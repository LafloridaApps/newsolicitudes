package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;
import java.util.List;

public class FeriadosLegalesDto {

    private Integer anio;
    private int diasCorresponden;
    private int diasAcumulados;
    private int total;
    private int diasTomados;
    private int diasPerdidos;
    private int diasPendientes;
    private List<DetalleFeriado> detalle;

    public static Builder builder() {
        return new Builder();
    }

    public FeriadosLegalesDto() {
    }

    private FeriadosLegalesDto(Builder builder) {
        this.anio = builder.anio;
        this.diasCorresponden = builder.diasCorresponden;
        this.diasAcumulados = builder.diasAcumulados;
        this.total = builder.total;
        this.diasTomados = builder.diasTomados;
        this.diasPerdidos = builder.diasPerdidos;
        this.diasPendientes = builder.diasPendientes;
        this.detalle = builder.detalle;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public int getDiasCorresponden() {
        return diasCorresponden;
    }

    public void setDiasCorresponden(int diasCorresponden) {
        this.diasCorresponden = diasCorresponden;
    }

    public int getDiasAcumulados() {
        return diasAcumulados;
    }

    public void setDiasAcumulados(int diasAcumulados) {
        this.diasAcumulados = diasAcumulados;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDiasTomados() {
        return diasTomados;
    }

    public void setDiasTomados(int diasTomados) {
        this.diasTomados = diasTomados;
    }

    public int getDiasPerdidos() {
        return diasPerdidos;
    }

    public void setDiasPerdidos(int diasPerdidos) {
        this.diasPerdidos = diasPerdidos;
    }

    public int getDiasPendientes() {
        return diasPendientes;
    }

    public void setDiasPendientes(int diasPendientes) {
        this.diasPendientes = diasPendientes;
    }

    public List<DetalleFeriado> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleFeriado> detalle) {
        this.detalle = detalle;
    }

    public static class DetalleFeriado {
        private Integer numero;
        private String resolucion;
        private LocalDate fechaResolucion;
        private LocalDate fechaInicio;
        private LocalDate fechaTermino;
        private double periodo;

        public DetalleFeriado(Integer numero, String resolucion, LocalDate fechaResolucion, LocalDate fechaInicio,
                LocalDate fechaTermino, double periodo) {
            this.numero = numero;
            this.resolucion = resolucion;
            this.fechaResolucion = fechaResolucion;
            this.fechaInicio = fechaInicio;
            this.fechaTermino = fechaTermino;
            this.periodo = periodo;
        }

        // Getters y Setters
        public Integer getNumero() {
            return numero;
        }

        public void setNumero(Integer numero) {
            this.numero = numero;
        }

        public String getResolucion() {
            return resolucion;
        }

        public void setResolucion(String resolucion) {
            this.resolucion = resolucion;
        }

        public LocalDate getFechaResolucion() {
            return fechaResolucion;
        }

        public void setFechaResolucion(LocalDate fechaResolucion) {
            this.fechaResolucion = fechaResolucion;
        }

        public LocalDate getFechaInicio() {
            return fechaInicio;
        }

        public void setFechaInicio(LocalDate fechaInicio) {
            this.fechaInicio = fechaInicio;
        }

        public LocalDate getFechaTermino() {
            return fechaTermino;
        }

        public void setFechaTermino(LocalDate fechaTermino) {
            this.fechaTermino = fechaTermino;
        }

        public double getPeriodo() {
            return periodo;
        }

        public void setPeriodo(double periodo) {
            this.periodo = periodo;
        }
    }

    public static class Builder {
        private Integer anio;
        private int diasCorresponden;
        private int diasAcumulados;
        private int total;
        private int diasTomados;
        private int diasPerdidos;
        private int diasPendientes;
        private List<DetalleFeriado> detalle;

        public Builder anio(Integer anio) {
            this.anio = anio;
            return this;
        }

        public Builder diasCorresponden(int v) {
            this.diasCorresponden = v;
            return this;
        }

        public Builder diasAcumulados(int v) {
            this.diasAcumulados = v;
            return this;
        }

        public Builder total(int v) {
            this.total = v;
            return this;
        }

        public Builder diasTomados(int v) {
            this.diasTomados = v;
            return this;
        }

        public Builder diasPerdidos(int v) {
            this.diasPerdidos = v;
            return this;
        }

        public Builder diasPendientes(int v) {
            this.diasPendientes = v;
            return this;
        }

        public Builder detalle(List<DetalleFeriado> v) {
            this.detalle = v;
            return this;
        }

        public FeriadosLegalesDto build() {
            return new FeriadosLegalesDto(this);
        }
    }
}
