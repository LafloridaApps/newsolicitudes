package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "anulaciones")
public class Anulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_anulacion_id", unique = true, nullable = false)
    private SolicitudAnulacion solicitudAnulacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", unique = true, nullable = false)
    private Solicitud solicitud;

    @Column(name = "fecha_anulacion", nullable = false)
    private LocalDate fechaAnulacion;

    @Column(name = "rut_aprobador")
    private Integer rutAprobador;

    @PrePersist
    public void prePersist() {
        if (this.fechaAnulacion == null) {
            this.fechaAnulacion = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolicitudAnulacion getSolicitudAnulacion() {
        return solicitudAnulacion;
    }

    public void setSolicitudAnulacion(SolicitudAnulacion solicitudAnulacion) {
        this.solicitudAnulacion = solicitudAnulacion;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public LocalDate getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDate fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public Integer getRutAprobador() {
        return rutAprobador;
    }

    public void setRutAprobador(Integer rutAprobador) {
        this.rutAprobador = rutAprobador;
    }

}
