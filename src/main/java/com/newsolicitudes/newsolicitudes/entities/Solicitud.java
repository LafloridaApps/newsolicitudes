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

    @ManyToOne
    @JoinColumn(name = "solicitante_id")
    private Funcionario solicitante;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @OneToMany(mappedBy = "solicitud")

    private List<Derivacion> derivaciones;

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
        POSTERGADA
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

    public Funcionario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Funcionario solicitante) {
        this.solicitante = solicitante;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
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

    public String getNombreSolicitante() {
        return solicitante.getNombre();

    }

    public String getNombreDepartamento() {
        return departamento.getNombreDepartamento();
    }

    public Integer getRutSolicitante() {
        return solicitante.getRut();
    }

}
