package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;
import java.util.List;

public class ApiFeriadosResponse {

    private Integer anio;
    private int diasCorresponden;
    private int diasAcumulados;
    private int total;
    private int diasTomados;
    private int diasPerdidos;
    private int diasPendientes;

    private List<DetalleFeriado> detalle;

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

    public static class DetalleFeriado {

        private Integer numero;
        private String resolucion;
        private LocalDate fechaResolucion;
        private LocalDate fechaInicio;
        private LocalDate fechaTermino;
        private double periodo;

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

    public List<DetalleFeriado> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleFeriado> detalle) {
        this.detalle = detalle;
    }

}
