package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class AdministrativoDto {

    private double maximo;
    private double usados;
    private double saldo;
    private int anio;
    private List<Detalle> detalle;

    public AdministrativoDto(double maximo, double usados, double saldo, int anio, List<Detalle> detalle) {
        this.maximo = maximo;
        this.usados = usados;
        this.saldo = saldo;
        this.anio = anio;
        this.detalle = detalle;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getMaximo() {
        return maximo;
    }

    public void setMaximo(double maximo) {
        this.maximo = maximo;
    }

    public double getUsados() {
        return usados;
    }

    public void setUsados(double usados) {
        this.usados = usados;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public List<Detalle> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<Detalle> detalle) {
        this.detalle = detalle;
    }

    // Clase interna Detalle
    public static class Detalle {
        private int numero;
        private String resolucion;
        private String fechaResolucion;
        private String fechaInicio;
        private String fechaTermino;
        private Double periodo;

        public Detalle() {}

        public Detalle(int numero, String resolucion, String fechaResolucion,
                       String fechaInicio, String fechaTermino, Double periodo) {
            this.numero = numero;
            this.resolucion = resolucion;
            this.fechaResolucion = fechaResolucion;
            this.fechaInicio = fechaInicio;
            this.fechaTermino = fechaTermino;
            this.periodo = periodo;
        }

        public int getNumero() {
            return numero;
        }

        public void setNumero(int numero) {
            this.numero = numero;
        }

        public String getResolucion() {
            return resolucion;
        }

        public void setResolucion(String resolucion) {
            this.resolucion = resolucion;
        }

        public String getFechaResolucion() {
            return fechaResolucion;
        }

        public void setFechaResolucion(String fechaResolucion) {
            this.fechaResolucion = fechaResolucion;
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

        public Double getPeriodo() {
            return periodo;
        }

        public void setPeriodo(Double periodo) {
            this.periodo = periodo;
        }
    }

    // Builder dentro de AdministrativoDto
    public static class Builder {
        private double maximo;
        private double usados;
        private double saldo;
        private int anio;
        private List<Detalle> detalle;

        public Builder maximo(double maximo) {
            this.maximo = maximo;
            return this;
        }

        public Builder usados(double usados) {
            this.usados = usados;
            return this;
        }

        public Builder saldo(double saldo) {
            this.saldo = saldo;
            return this;
        }

        public Builder anio(int anio) {
            this.anio = anio;
            return this;
        }

        public Builder detalle(List<Detalle> detalle) {
            this.detalle = detalle;
            return this;
        }

        public AdministrativoDto build() {
            return new AdministrativoDto(maximo, usados, saldo, anio, detalle);
        }
    }
}
