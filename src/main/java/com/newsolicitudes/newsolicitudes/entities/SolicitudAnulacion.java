package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitud_anulacion")
public class SolicitudAnulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitudAnulacion estado;

    @Column(name = "rut_solicitante")
    private Integer rutSolicitante;

    public enum EstadoSolicitudAnulacion {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = FechaUtils.fechaActual();
        }
        if (this.estado == null) {
            this.estado = EstadoSolicitudAnulacion.PENDIENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitudAnulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudAnulacion estado) {
        this.estado = estado;
    }

    public Integer getRutSolicitante() {
        return rutSolicitante;
    }

    public void setRutSolicitante(Integer rutSolicitante) {
        this.rutSolicitante = rutSolicitante;
    }

}
