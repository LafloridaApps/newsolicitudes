package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class UsuarioModuloRequest {

    private String rut;
    private List<Long> modulos;

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public List<Long> getModulos() {
        return modulos;
    }

    public void setModulos(List<Long> modulos) {
        this.modulos = modulos;
    }
}
