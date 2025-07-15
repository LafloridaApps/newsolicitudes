package com.newsolicitudes.newsolicitudes.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DepartamentoList {

    private Long id;
    private String nombre;
    private String nivel;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DepartamentoList> dependencias = new ArrayList<>();
    private String nombreJefe;
    private Integer rutJefe;
    private String vrutJefe;
    private String email;

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

    public List<DepartamentoList> getDependencias() {
        return dependencias;
    }

    public void setDependencias(List<DepartamentoList> dependencias) {
        this.dependencias = dependencias;
    }

    public String getNombreJefe() {
        return nombreJefe;
    }

    public void setNombreJefe(String nombreJefe) {
        this.nombreJefe = nombreJefe;
    }

    public Integer getRutJefe() {
        return rutJefe;
    }

    public void setRutJefe(Integer rutJefe) {
        this.rutJefe = rutJefe;
    }

    public String getVrutJefe() {
        return vrutJefe;
    }

    public void setVrutJefe(String vrutJefe) {
        this.vrutJefe = vrutJefe;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
