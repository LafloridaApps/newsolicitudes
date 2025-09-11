package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class DepartamentoJerarquiaDTO {
    private Long id;
    private String nombre;
    private String nivel;
    private List<DepartamentoJerarquiaDTO> hijos;

    public DepartamentoJerarquiaDTO(Long id, String nombre, String nivel, List<DepartamentoJerarquiaDTO> hijos) {
        this.id = id;
        this.nombre = nombre;
        this.nivel = nivel;
        this.hijos = hijos;
    }

    // Getters y Setters
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

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public List<DepartamentoJerarquiaDTO> getHijos() {
        return hijos;
    }

    public void setHijos(List<DepartamentoJerarquiaDTO> hijos) {
        this.hijos = hijos;
    }
}
