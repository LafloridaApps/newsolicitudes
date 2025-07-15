package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class ApiAdministrativoResponse {

    private double maximo;
    private double usados;
    private double saldo;
    private int anio;
    private List<Detalle> detalle;

    // Clases internas

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

   

   

    public static class Detalle {
        private int numero;
        private String resolucion;

        private String fechaResolucion;

        private String fechaInicio;

        private String fechaTermino;

        private Double periodo;

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

        public Double getPeriodo() {
            return periodo;
        }

        public void setPeriodo(Double periodo) {
            this.periodo = periodo;
        }

    }





    public List<Detalle> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<Detalle> detalle) {
        this.detalle = detalle;
    }
}
