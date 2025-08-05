package com.newsolicitudes.newsolicitudes.dto;

public class DepartamentoResponse {

    private Long id;
    private String nombre;
    private Integer rutJefe;
    private Integer rutJefeSuperior;
    private Long idDeptoSuperior;
    private String nivelDepartamento;

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

    public Integer getRutJefe() {
        return rutJefe;
    }

    public void setRutJefe(Integer rutJefe) {
        this.rutJefe = rutJefe;
    }

    public Integer getRutJefeSuperior() {
        return rutJefeSuperior;
    }

    public void setRutJefeSuperior(Integer rutJefeSuperior) {
        this.rutJefeSuperior = rutJefeSuperior;
    }

    public Long getIdDeptoSuperior() {
        return idDeptoSuperior;
    }

    public void setIdDeptoSuperior(Long idDeptoSuperior) {
        this.idDeptoSuperior = idDeptoSuperior;
    }

    public String getNivelDepartamento() {
        return nivelDepartamento;
    }

    public void setNivelDepartamento(String nivelDepartamento) {
        this.nivelDepartamento = nivelDepartamento;
    }

}
