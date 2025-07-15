package com.newsolicitudes.newsolicitudes.dto;

public class DepartamentoResponse {

    private Long id;
    private String nombre;

    public DepartamentoResponse() {
    }

    public DepartamentoResponse(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
