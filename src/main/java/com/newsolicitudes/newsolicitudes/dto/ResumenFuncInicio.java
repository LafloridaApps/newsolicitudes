package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;
import java.util.List;

public class ResumenFuncInicio {

    private int saldoFeriado;
    private double saldoAdministrativo;
    private Long idUltimaSolicitud;
    private String estadoUltimaSolicitud;
    private List<SolicitudMes> solicitudMes;

    public int getSaldoFeriado() {
        return saldoFeriado;
    }

    public void setSaldoFeriado(int saldoFeriado) {
        this.saldoFeriado = saldoFeriado;
    }

    public double getSaldoAdministrativo() {
        return saldoAdministrativo;
    }

    public void setSaldoAdministrativo(double saldoAdministrativo) {
        this.saldoAdministrativo = saldoAdministrativo;
    }

    public Long getIdUltimaSolicitud() {
        return idUltimaSolicitud;
    }

    public void setIdUltimaSolicitud(Long idUltimaSolicitud) {
        this.idUltimaSolicitud = idUltimaSolicitud;
    }

    public String getEstadoUltimaSolicitud() {
        return estadoUltimaSolicitud;
    }

    public void setEstadoUltimaSolicitud(String estadoUltimaSolicitud) {
        this.estadoUltimaSolicitud = estadoUltimaSolicitud;
    }

    public List<SolicitudMes> getSolicitudMes() {
        return solicitudMes;
    }

    public void setSolicitudMes(List<SolicitudMes> solicitudMes) {
        this.solicitudMes = solicitudMes;
    }

    public static class SolicitudMes {
        private Long idSolicitud;
        private String tipoSolicitud;
        private String estado;
        private LocalDate fechaSolicitud;

        public SolicitudMes(String tipoSolicitud, String estado, LocalDate fechaSolicitud, Long idSolicitud) {
            this.tipoSolicitud = tipoSolicitud;
            this.estado = estado;
            this.fechaSolicitud = fechaSolicitud;
            this.idSolicitud = idSolicitud;
        }

        public String getTipoSolicitud() {
            return tipoSolicitud;
        }

        public void setTipoSolicitud(String tipoSolicitud) {
            this.tipoSolicitud = tipoSolicitud;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public LocalDate getFechaSolicitud() {
            return fechaSolicitud;
        }

        public void setFechaSolicitud(LocalDate fechaSolicitud) {
            this.fechaSolicitud = fechaSolicitud;
        }

        public Long getIdSolicitud() {
            return idSolicitud;
        }

        public void setIdSolicitud(Long idSolicitud) {
            this.idSolicitud = idSolicitud;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int saldoFeriado;
        private double saldoAdministrativo;
        private Long idUltimaSolicitud;
        private String estadoUltimaSolicitud;
        private List<SolicitudMes> solicitudMes;

        public Builder saldoFeriado(int saldoFeriado) {
            this.saldoFeriado = saldoFeriado;
            return this;
        }

        public Builder saldoAdministrativo(double saldoAdministrativo) {
            this.saldoAdministrativo = saldoAdministrativo;
            return this;
        }

        public Builder idUltimaSolicitud(Long idUltimaSolicitud) {
            this.idUltimaSolicitud = idUltimaSolicitud;
            return this;
        }

        public Builder estadoUltimaSolicitud(String estadoUltimaSolicitud) {
            this.estadoUltimaSolicitud = estadoUltimaSolicitud;
            return this;
        }

        public Builder solicitudMes(List<SolicitudMes> solicitudMes) {
            this.solicitudMes = solicitudMes;
            return this;
        }

        public ResumenFuncInicio build() {
            ResumenFuncInicio resumenFuncInicio = new ResumenFuncInicio();
            resumenFuncInicio.setSaldoFeriado(saldoFeriado);
            resumenFuncInicio.setSaldoAdministrativo(saldoAdministrativo);
            resumenFuncInicio.setIdUltimaSolicitud(idUltimaSolicitud);
            resumenFuncInicio.setEstadoUltimaSolicitud(estadoUltimaSolicitud);
            resumenFuncInicio.setSolicitudMes(solicitudMes);
            return resumenFuncInicio;
        }

    }

}
