package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class LicenciaDeptoDto {
    private String nombreGrupo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private String depto;
    private int ident;
    private Integer rut;

    public LicenciaDeptoDto() {
    }

    public LicenciaDeptoDto(String nombreGrupo, String nombre, LocalDate fechaInicio, LocalDate fechaTermino,
            String depto, int ident, Integer rut) {
        this.nombreGrupo = nombreGrupo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaTermino = fechaTermino;
        this.depto = depto;
        this.ident = ident;
        this.rut = rut;
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

    public String getDepto() {
        return depto;
    }

    public void setDepto(String depto) {
        this.depto = depto;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }
}
