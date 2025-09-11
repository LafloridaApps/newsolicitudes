package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class DashboardAusenciaDto {
    private String nombreGrupo;
    private String nombre;
    private String rut;
    private String motivo;
    private String idSolicitud;
    private LocalDate fechaAprobacion;
    private PeriodoDto periodoAusencia;

    public DashboardAusenciaDto(String nombreGrupo, String nombre, String rut, String motivo, String idSolicitud,
            LocalDate fechaAprobacion, PeriodoDto periodoAusencia) {
        this.nombreGrupo = nombreGrupo;
        this.nombre = nombre;
        this.rut = rut;
        this.motivo = motivo;
        this.idSolicitud = idSolicitud;
        this.fechaAprobacion = fechaAprobacion;
        this.periodoAusencia = periodoAusencia;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(String idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public LocalDate getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDate fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public PeriodoDto getPeriodoAusencia() {
        return periodoAusencia;
    }

    public void setPeriodoAusencia(PeriodoDto periodoAusencia) {
        this.periodoAusencia = periodoAusencia;
    }
}
