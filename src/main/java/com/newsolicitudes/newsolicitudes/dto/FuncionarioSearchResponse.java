package com.newsolicitudes.newsolicitudes.dto;

public class FuncionarioSearchResponse {

    private String nombre;
    private Integer rut;
    private String vrut;
    private String departamento;
    private Integer ident;
    private boolean isAusente;

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

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getVrut() {
        return vrut;
    }

    public void setVrut(String vrut) {
        this.vrut = vrut;
    }

    public Integer getIdent() {
        return ident;
    }

    public void setIdent(Integer ident) {
        this.ident = ident;
    }


    public boolean isAusente() {
        return isAusente;
    }

    public void setIsAusente(boolean isAusente) {
        this.isAusente = isAusente;
    }
 

}
