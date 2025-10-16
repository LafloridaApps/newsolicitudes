package com.newsolicitudes.newsolicitudes.dto;

public class FirmaFuncionarioDto {

    private Long id;
    private String nombre;
    private Integer rut;
    private String dv;
    private EstadoFirma estadoFirma;
    private Long diasRestantes;

    public enum EstadoFirma {
        VIGENTE, VENCIDA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public String getDv() {
        return dv;
    }

    public void setDv(String dv) {
        this.dv = dv;
    }

    public EstadoFirma getEstadoFirma() {
        return estadoFirma;
    }

    public void setEstadoFirma(EstadoFirma estadoFirma) {
        this.estadoFirma = estadoFirma;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(Long diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

}
