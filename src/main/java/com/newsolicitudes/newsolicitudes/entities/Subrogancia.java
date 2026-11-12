package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
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

    private Long solicitudId;

    

    private String ceroPapelEstado;

    private Long ceroPapelPerfilId;

    private Long ceroPapelTareaId;

    private Long ceroPapelSubroganciaId;

    @Column(length = 2000)
    private String ceroPapelDetalle;

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

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public Long getIdDepto() {
        return idDepto;
    }

    public void setIdDepto(Long idDepto) {
        this.idDepto = idDepto;
    }

    public String getCeroPapelEstado() {
        return ceroPapelEstado;
    }

    public void setCeroPapelEstado(String ceroPapelEstado) {
        this.ceroPapelEstado = ceroPapelEstado;
    }

    public Long getCeroPapelPerfilId() {
        return ceroPapelPerfilId;
    }

    public void setCeroPapelPerfilId(Long ceroPapelPerfilId) {
        this.ceroPapelPerfilId = ceroPapelPerfilId;
    }

    public Long getCeroPapelTareaId() {
        return ceroPapelTareaId;
    }

    public void setCeroPapelTareaId(Long ceroPapelTareaId) {
        this.ceroPapelTareaId = ceroPapelTareaId;
    }

    public Long getCeroPapelSubroganciaId() {
        return ceroPapelSubroganciaId;
    }

    public void setCeroPapelSubroganciaId(Long ceroPapelSubroganciaId) {
        this.ceroPapelSubroganciaId = ceroPapelSubroganciaId;
    }

    public String getCeroPapelDetalle() {
        return ceroPapelDetalle;
    }

    public void setCeroPapelDetalle(String ceroPapelDetalle) {
        this.ceroPapelDetalle = ceroPapelDetalle;
    }

}
