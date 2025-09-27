package com.newsolicitudes.newsolicitudes.dto;

import java.util.List; // Importar List

public class FuncionarioResponseApi {

    private Integer rut;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String vrut;
    private String email;
    private String departamento;
    private Long codDepto;
    private String nombreJefe;
    private Long codDeptoJefe;
    private String foto;
    private Integer ident;
    private String tipoContrato;
    private String escalafon;
    private Integer grado;

    public String getEscalafon() {
        return escalafon;
    }

    public void setEscalafon(String escalafon) {
        this.escalafon = escalafon;
    }

    public Integer getGrado() {
        return grado;
    }

    public void setGrado(Integer grado) {
        this.grado = grado;
    }

    private List<DecretoDto> decretos; // Nuevo campo

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getVrut() {
        return vrut;
    }

    public void setVrut(String vrut) {
        this.vrut = vrut;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public Long getCodDepto() {
        return codDepto;
    }

    public void setCodDepto(Long codDepto) {
        this.codDepto = codDepto;
    }

    public String getNombreJefe() {
        return nombreJefe;
    }

    public void setNombreJefe(String nombreJefe) {
        this.nombreJefe = nombreJefe;
    }

    public Long getCodDeptoJefe() {
        return codDeptoJefe;
    }

    public void setCodDeptoJefe(Long codDeptoJefe) {
        this.codDeptoJefe = codDeptoJefe;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Integer getIdent() {
        return ident;
    }

    public void setIdent(Integer ident) {
        this.ident = ident;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    // Getters y setters para el nuevo campo 'decretos'
    public List<DecretoDto> getDecretos() {
        return decretos;
    }

    public void setDecretos(List<DecretoDto> decretos) {
        this.decretos = decretos;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidoPaterno + " " + apellidoMaterno;
    }

    public String getRutCompleto() {
        return rut + "-" + vrut;
    }
}