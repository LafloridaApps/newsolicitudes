package com.newsolicitudes.newsolicitudes.dto;


public class AprobacionList {

    private Long idSolicitud;
    private String rut;
    private String nombres;
    private String apellidos;
    private String departamento;
    private String jornada;
    private String desde;
    private String hasta;
    private double duracion;
    private String fechaSolicitud;
    private String tipoSolicitud;
    private String tipoContrato;
    private String url;

    private AprobacionList(Builder builder) {
        this.idSolicitud = builder.idSolicitud;
        this.rut = builder.rut;
        this.nombres = builder.nombres;
        this.apellidos = builder.apellidos;
        this.departamento = builder.departamento;
        this.jornada = builder.jornada;
        this.desde = builder.desde;
        this.hasta = builder.hasta;
        this.duracion = builder.duracion;
        this.fechaSolicitud = builder.fechaSolicitud;
        this.tipoSolicitud = builder.tipoSolicitud;
        this.tipoContrato = builder.tipoContrato;
        this.url = builder.url;

    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public double getDuracion() {
        return duracion;
    }

    public void setDuracion(double duracion) {
        this.duracion = duracion;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }
    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }

    public static class Builder {

        private Long idSolicitud;
        private String rut;
        private String nombres;
        private String apellidos;
        private String departamento;
        private String jornada;
        private double duracion;
        private String desde;
        private String hasta;
        private String fechaSolicitud;
        private String tipoSolicitud;
        private String tipoContrato;
        private String url;

        public Builder idSolicitud(Long idSolicitud) {
            this.idSolicitud = idSolicitud;
            return this;
        }

        public Builder rut(String rut) {
            this.rut = rut;
            return this;
        }

        public Builder nombres(String nombres) {
            this.nombres = nombres;
            return this;
        }

        public Builder apellidos(String apellidos) {
            this.apellidos = apellidos;
            return this;
        }

        public Builder departamento(String departamento) {
            this.departamento = departamento;
            return this;
        }

        public Builder jornada(String jornada) {
            this.jornada = jornada;
            return this;
        }

        public Builder duracion(double duracion) {
            this.duracion = duracion;
            return this;
        }

        public Builder fechaSolicitud(String fechaSolicitud) {
            this.fechaSolicitud = fechaSolicitud;
            return this;
        }

        public Builder tipoSolicitud(String tipoSolicitud) {
            this.tipoSolicitud = tipoSolicitud;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder tipoContrato(String tipoContrato) {
            this.tipoContrato = tipoContrato;
            return this;
        }

        public Builder desde(String desde) {
            this.desde = desde;
            return this;
        }

        public Builder hasta(String hasta) {
            this.hasta = hasta;
            return this;
        }

        public AprobacionList build() {
            return new AprobacionList(this);
        }

    }

}
