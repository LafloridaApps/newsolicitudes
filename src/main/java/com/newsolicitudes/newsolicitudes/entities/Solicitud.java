package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rut;

    private Long idDepto;

    @OneToMany(mappedBy = "solicitud")
    private List<Derivacion> derivaciones;

    @ManyToOne
    @JoinColumn(name = "decreto_id")
    private Decreto decreto;

    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private LocalDate fechaInicio;

    private LocalDate fechaTermino;

    private TipoSolicitud tipoSolicitud;

    private Double cantidadDias;

    private Jornada jornadaInicio;
    private Jornada jornadaTermino;

    public enum TipoSolicitud {
        ADMINISTRATIVO,
        FERIADO
    }

    public enum EstadoSolicitud {
        PENDIENTE,
        APROBADA,
        POSTERGADA,
        DECRETADA
    }

    public enum Jornada {
        COMPLETA,
        AM,
        PM
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(LocalDate fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(TipoSolicitud tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public Double getCantidadDias() {
        return cantidadDias;
    }

    public void setCantidadDias(Double cantidadDias) {
        this.cantidadDias = cantidadDias;
    }

    public Jornada getJornadaInicio() {
        return jornadaInicio;
    }

    public void setJornadaInicio(Jornada jornadaInicio) {
        this.jornadaInicio = jornadaInicio;
    }

    public Jornada getJornadaTermino() {
        return jornadaTermino;
    }

    public void setJornadaTermino(Jornada jornadaTermino) {
        this.jornadaTermino = jornadaTermino;
    }

    public List<Derivacion> getDerivaciones() {
        return derivaciones;
    }

    public void setDerivaciones(List<Derivacion> derivaciones) {
        this.derivaciones = derivaciones;
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public Long getIdDepto() {
        return idDepto;
    }

    public void setIdDepto(Long idDepto) {
        this.idDepto = idDepto;
    }

    public Decreto getDecreto() {
        return decreto;
    }

    public void setDecreto(Decreto decreto) {
        this.decreto = decreto;
    }
}
