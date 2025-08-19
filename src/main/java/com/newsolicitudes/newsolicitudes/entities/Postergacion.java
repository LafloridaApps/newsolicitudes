package com.newsolicitudes.newsolicitudes.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;

@Entity
public class Postergacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "solicitud_id", referencedColumnName = "id")
    private Solicitud solicitud;

    private LocalDate fechaPostergacion;

    private Integer rutPostergacion;

    private String glosa;

    public Postergacion() {
    }

    public Postergacion(Solicitud solicitud, LocalDate fechaPostergacion, Integer rutPostergacion, String glosa) {
        this.solicitud = solicitud;
        this.fechaPostergacion = fechaPostergacion;
        this.rutPostergacion = rutPostergacion;
        this.glosa = glosa;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public LocalDate getFechaPostergacion() {
        return fechaPostergacion;
    }

    public void setFechaPostergacion(LocalDate fechaPostergacion) {
        this.fechaPostergacion = fechaPostergacion;
    }

    public Integer getRutPostergacion() {
        return rutPostergacion;
    }

    public void setRutPostergacion(Integer rutPostergacion) {
        this.rutPostergacion = rutPostergacion;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }
}
