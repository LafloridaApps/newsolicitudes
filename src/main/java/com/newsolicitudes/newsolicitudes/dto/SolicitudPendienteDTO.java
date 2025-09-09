package com.newsolicitudes.newsolicitudes.dto;

public class SolicitudPendienteDTO {
    private String nombreFuncionario;
    private String tipoSolicitud; // Ej: Feriado, Permiso

    public SolicitudPendienteDTO(String nombreFuncionario, String tipoSolicitud) {
        this.nombreFuncionario = nombreFuncionario;
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getNombreFuncionario() {
        return nombreFuncionario;
    }

    public void setNombreFuncionario(String nombreFuncionario) {
        this.nombreFuncionario = nombreFuncionario;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }
}