package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Derivacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    private TipoDerivacion tipo;

    @ManyToOne(optional = false)
    private Departamento departamento;

    private LocalDate fechaDerivacion;

    @Enumerated(EnumType.STRING)
    private EstadoDerivacion estadoDerivacion;

    @OneToOne(mappedBy = "derivacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private EntradaDerivacion entrada;

    public enum TipoDerivacion {
        VISACION,
        FIRMA
    }

    public enum EstadoDerivacion {
        PENDIENTE,
        DERIVADA,
        FINALIZADA
    }

    // Getters y Setters

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

    public TipoDerivacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoDerivacion tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFechaDerivacion() {
        return fechaDerivacion;
    }

    public void setFechaDerivacion(LocalDate fechaDerivacion) {
        this.fechaDerivacion = fechaDerivacion;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public EstadoDerivacion getEstadoDerivacion() {
        return estadoDerivacion;
    }

    public void setEstadoDerivacion(EstadoDerivacion estadoDerivacion) {
        this.estadoDerivacion = estadoDerivacion;
    }

    public EntradaDerivacion getEntrada() {
        return entrada;
    }

    public void setEntrada(EntradaDerivacion entrada) {
        this.entrada = entrada;
    }

    // Métodos auxiliares (se mantienen)

    public String getNombreDepartamento() {
        return departamento != null ? departamento.getNombreDepartamento() : null;
    }

    public LocalDate getFechaSolicitud() {
        return solicitud != null ? solicitud.getFechaSolicitud() : null;
    }

    public LocalDate getFechaInicioSolicitud() {
        return solicitud != null ? solicitud.getFechaInicio() : null;
    }

    public LocalDate getFechaTerminoSolicitud() {
        return solicitud != null ? solicitud.getFechaTermino() : null;
    }

    public String getTipoSolicitud() {
        return solicitud != null ? solicitud.getTipoSolicitud().name() : null;
    }

    public String getEstadoSolicitud() {
        return solicitud != null ? solicitud.getEstado().name() : null;
    }

    public String getJornadaInicioSolicitud() {
        return solicitud != null && solicitud.getJornadaInicio() != null
            ? solicitud.getJornadaInicio().name()
            : null;
    }

    public String getJornadaTerminoSolicitud() {
        return solicitud != null && solicitud.getJornadaTermino() != null
            ? solicitud.getJornadaTermino().name()
            : null;
    }

    public Long getIdSolicitud() {
        return solicitud != null ? solicitud.getId() : null;
    }

    public String getNombreDepartamentoSolicitud() {
        return solicitud != null ? solicitud.getNombreDepartamento() : null;
    }
}
