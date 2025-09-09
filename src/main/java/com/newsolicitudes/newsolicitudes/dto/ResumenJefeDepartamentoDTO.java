package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class ResumenJefeDepartamentoDTO {

    private List<DepartamentoSubrogadoDTO> departamentosSubrogados;
    private List<SolicitudPendienteDTO> solicitudesPendientes;
    private List<ProximaAusenciaDTO> proximasAusencias;
    private Integer ausenciasEquipoHoy; // Nuevo campo

    // Constructor, getters y setters
    public ResumenJefeDepartamentoDTO(List<DepartamentoSubrogadoDTO> departamentosSubrogados,
                                      List<SolicitudPendienteDTO> solicitudesPendientes,
                                      List<ProximaAusenciaDTO> proximasAusencias,
                                      Integer ausenciasEquipoHoy) {
        this.departamentosSubrogados = departamentosSubrogados;
        this.solicitudesPendientes = solicitudesPendientes;
        this.proximasAusencias = proximasAusencias;
        this.ausenciasEquipoHoy = ausenciasEquipoHoy;
    }

    public List<DepartamentoSubrogadoDTO> getDepartamentosSubrogados() {
        return departamentosSubrogados;
    }

    public void setDepartamentosSubrogados(List<DepartamentoSubrogadoDTO> departamentosSubrogados) {
        this.departamentosSubrogados = departamentosSubrogados;
    }

    public List<SolicitudPendienteDTO> getSolicitudesPendientes() {
        return solicitudesPendientes;
    }

    public void setSolicitudesPendientes(List<SolicitudPendienteDTO> solicitudesPendientes) {
        this.solicitudesPendientes = solicitudesPendientes;
    }

    public List<ProximaAusenciaDTO> getProximasAusencias() {
        return proximasAusencias;
    }

    public void setProximasAusencias(List<ProximaAusenciaDTO> proximasAusencias) {
        this.proximasAusencias = proximasAusencias;
    }

    // Nuevo getter y setter para ausenciasEquipoHoy
    public Integer getAusenciasEquipoHoy() {
        return ausenciasEquipoHoy;
    }

    public void setAusenciasEquipoHoy(Integer ausenciasEquipoHoy) {
        this.ausenciasEquipoHoy = ausenciasEquipoHoy;
    }
}