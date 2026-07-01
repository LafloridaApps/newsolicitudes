package com.newsolicitudes.newsolicitudes.dto;

public class KpisDto {

    private long totalAnual;
    private long aprobadas;
    private long pendientes;
    private long rechazadas;
    private long postergadas;

    public long getTotalAnual() {
        return totalAnual;
    }

    public void setTotalAnual(long totalAnual) {
        this.totalAnual = totalAnual;
    }

    public long getAprobadas() {
        return aprobadas;
    }

    public void setAprobadas(long aprobadas) {
        this.aprobadas = aprobadas;
    }

    public long getPendientes() {
        return pendientes;
    }

    public void setPendientes(long pendientes) {
        this.pendientes = pendientes;
    }

    public long getRechazadas() {
        return rechazadas;
    }

    public void setRechazadas(long rechazadas) {
        this.rechazadas = rechazadas;
    }

    public long getPostergadas() {
        return postergadas;
    }

    public void setPostergadas(long postergadas) {
        this.postergadas = postergadas;
    }
}