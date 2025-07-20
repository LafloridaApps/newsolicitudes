package com.newsolicitudes.newsolicitudes.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreDepartamento;

    @ManyToOne
    private Departamento departamentoSuperior;

    private boolean activo;

    @ManyToOne
    private Funcionario jefe;

    @Enumerated(EnumType.STRING)
    private NivelDepartamento nivel;

    @OneToMany(mappedBy = "departamentoSuperior")
    private List<Departamento> childrens = new ArrayList<>();

    @OneToMany(mappedBy = "departamento")
    private List<Funcionario> funcionarios;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    public Departamento getDepartamentoSuperior() {
        return departamentoSuperior;
    }

    public void setDepartamentoSuperior(Departamento departamentoPadre) {
        this.departamentoSuperior = departamentoPadre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public NivelDepartamento getNivel() {
        return nivel;
    }

    public Funcionario getJefe() {
        return jefe != null ? jefe : null;
    }

    public void setJefe(Funcionario jefe) {
        this.jefe = jefe;
    }

    public void setChildrens(List<Departamento> childrens) {
        this.childrens = childrens;
    }

    public void setNivel(NivelDepartamento nivel) {
        this.nivel = nivel;
    }

    public enum NivelDepartamento {
        ALCALDIA,
        ADMINISTRACION,
        DIRECCION,
        SUBDIRECCION,
        DEPARTAMENTO,
        SECCION,
        OFICINA

    }

    public List<Departamento> getChildrens() {
        return childrens;
    }

    public Long idPadre() {
        return this.getDepartamentoSuperior().getId();
    }

    public Integer getRutJefe() {
        return this.jefe != null && this.jefe.getRut() != null ? this.jefe.getRut() : null;
    }

    public NivelDepartamento getNivelDeprtamentoSuperior() {
        return this.departamentoSuperior != null ? this.departamentoSuperior.getNivel() : null;
    }

    public Character getVrutJefe() {
        return this.jefe != null && this.jefe.getRut() != null ? this.jefe.getVrut() : null;
    }

    public Funcionario jefeDepartamentoSuperior() {
        return this.departamentoSuperior != null ? this.departamentoSuperior.getJefe() : null;
    }

    public String getNombreJefe() {
        return this.jefe != null ? this.jefe.getNombre() : null;
    }

    public String getEmailFuncionario() {
        return this.jefe != null ? this.jefe.getEmail() : null;
    }

}