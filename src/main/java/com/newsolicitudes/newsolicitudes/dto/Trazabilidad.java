package com.newsolicitudes.newsolicitudes.dto;

import com.newsolicitudes.newsolicitudes.entities.enums.EstadoTrazabilidad;

public class Trazabilidad {

    private Long id;
    private String fecha;
    private String accion;
    private String usuario;
    private String departamento;
    private EstadoTrazabilidad estado;
    private String glosa;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public EstadoTrazabilidad getEstado() {
        return estado;
    }

    public void setEstado(EstadoTrazabilidad estado) {
        this.estado = estado;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

}
