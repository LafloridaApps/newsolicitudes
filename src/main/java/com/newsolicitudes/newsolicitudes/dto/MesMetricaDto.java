package com.newsolicitudes.newsolicitudes.dto;

public class MesMetricaDto {

    private String mes;
    private Long cantidad;

    public MesMetricaDto() {
    }

    public MesMetricaDto(String mes, Long cantidad) {
        this.mes = mes;
        this.cantidad = cantidad;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }
}