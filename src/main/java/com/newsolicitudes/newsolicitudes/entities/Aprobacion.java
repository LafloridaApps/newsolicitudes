package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Aprobacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    private Solicitud solicitud;

    private Integer rut;

    private LocalDate fechaAprobacion;

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

    public LocalDate getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDate fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public Long getIdSolicitud() {
        return solicitud.getId();
    }

    public Integer getRutSolicitud() {
        return solicitud.getRut();
    }

    public Long getDeptoSolicitud() {
        return solicitud.getIdDepto();
    }

    public LocalDate getFechaSolicitud() {
        return solicitud.getFechaSolicitud();
    }

    public LocalDate getFechaInicioSolicitud() {
        return solicitud.getFechaInicio();
    }

    public LocalDate getFechaTerminoSolicitud() {
        return solicitud.getFechaTermino();
    }

    public double getDuracionSolicitud() {
        return solicitud.getCantidadDias();
    }

    public String getTipoSolicitud() {
        return solicitud.getTipoSolicitud().name();
    }

    public EstadoSolicitud getEstadoSolicitud() {
        return solicitud.getEstado();
    }

}