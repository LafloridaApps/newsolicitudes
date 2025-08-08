package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Subrogancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer subrogante;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Integer jefeDepartamento;

    private Long idDepto;

    

    public Subrogancia() {
    }

    public Subrogancia(Integer subrogante, LocalDate fechaInicio, LocalDate fechaFin, Integer jefeDepartamento,
            Long idDepto) {
        this.subrogante = subrogante;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.jefeDepartamento = jefeDepartamento;
        this.idDepto = idDepto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getSubrogante() {
        return subrogante;
    }

    public void setSubrogante(Integer subrogante) {
        this.subrogante = subrogante;
    }

    public Integer getJefeDepartamento() {
        return jefeDepartamento;
    }

    public void setJefeDepartamento(Integer jefeDepartamento) {
        this.jefeDepartamento = jefeDepartamento;
    }

    public Long getIdDepto() {
        return idDepto;
    }

    public void setIdDepto(Long idDepto) {
        this.idDepto = idDepto;
    }

}