package com.newsolicitudes.newsolicitudes.dto;

public class DepartamentoMetricaDto {

    private String nombreDepto;
    private Long cantidad;
    private String color;

    public DepartamentoMetricaDto(String nombreDepto, Long cantidad, String color) {
        this.nombreDepto = nombreDepto;
        this.cantidad = cantidad;
        this.color = color;
    }

    public String getNombreDepto() {
        return nombreDepto;
    }

    public void setNombreDepto(String nombreDepto) {
        this.nombreDepto = nombreDepto;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}