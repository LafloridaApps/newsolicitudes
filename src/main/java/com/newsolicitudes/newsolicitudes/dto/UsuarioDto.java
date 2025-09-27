package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class UsuarioDto {

    private Long idUsuario;
    private String login;
    private Integer rut;
    private String vrut;

    private String nombre;
    private List<ModuloDto> modulos;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getRut() {
        return rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<ModuloDto> getModulos() {
        return modulos;
    }

    public void setModulos(List<ModuloDto> modulos) {
        this.modulos = modulos;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public String getVrut() {
        return vrut;
    }

    public void setVrut(String vrut) {
        this.vrut = vrut;
    }

}
