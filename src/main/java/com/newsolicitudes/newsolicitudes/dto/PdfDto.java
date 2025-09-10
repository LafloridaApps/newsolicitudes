package com.newsolicitudes.newsolicitudes.dto;

public class PdfDto {

    private Long idSol;
    private Long tipoSolicitud;
    private String jornada;
    private String nroIniDia;
    private String mesIni;
    private String nroFinDia;
    private String mesFin;
    private String diasTomados;
    private String rut;
    private String vrut;
    private String paterno;
    private String materno;
    private String nombres;
    private String depto;
    private String escalafon;
    private String grado;
    private String telefono;
    private String rutJefe;
    private String nombreJefe;
    private String rutDirector;
    private String nombreDirector;
    private String nombreSolicitud;

    private PdfDto(Builder builder) {
        this.idSol = builder.idSol;
        this.jornada = builder.jornada;
        this.nroIniDia = builder.nroIniDia;
        this.mesIni = builder.mesIni;
        this.nroFinDia = builder.nroFinDia;
        this.mesFin = builder.mesFin;
        this.diasTomados = builder.diasTomados;
        this.rut = builder.rut;
        this.vrut = builder.vrut;
        this.paterno = builder.paterno;
        this.materno = builder.materno;
        this.nombres = builder.nombres;
        this.depto = builder.depto;
        this.escalafon = builder.escalafon;
        this.grado = builder.grado;
        this.telefono = builder.telefono;
        this.rutJefe = builder.rutJefe;
        this.nombreJefe = builder.nombreJefe;
        this.rutDirector = builder.rutDirector;
        this.nombreDirector = builder.nombreDirector;
        this.tipoSolicitud = builder.tipoSolicitud;
        this.nombreSolicitud = builder.nombreSolicitud;

    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getIdSol() {
        return idSol;
    }

    public void setIdSol(Long idSol) {
        this.idSol = idSol;
    }

    public Long getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(Long tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public String getNroIniDia() {
        return nroIniDia;
    }

    public void setNroIniDia(String nroIniDia) {
        this.nroIniDia = nroIniDia;
    }

    public String getMesIni() {
        return mesIni;
    }

    public void setMesIni(String mesIni) {
        this.mesIni = mesIni;
    }

    public String getNroFinDia() {
        return nroFinDia;
    }

    public void setNroFinDia(String nroFinDia) {
        this.nroFinDia = nroFinDia;
    }

    public String getMesFin() {
        return mesFin;
    }

    public void setMesFin(String mesFin) {
        this.mesFin = mesFin;
    }

    public String getDiasTomados() {
        return diasTomados;
    }

    public void setDiasTomados(String diasTomados) {
        this.diasTomados = diasTomados;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getVrut() {
        return vrut;
    }

    public void setVrut(String vrut) {
        this.vrut = vrut;
    }

    public String getPaterno() {
        return paterno;
    }

    public void setPaterno(String paterno) {
        this.paterno = paterno;
    }

    public String getMaterno() {
        return materno;
    }

    public void setMaterno(String materno) {
        this.materno = materno;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getDepto() {
        return depto;
    }

    public void setDepto(String depto) {
        this.depto = depto;
    }

    public String getEscalafon() {
        return escalafon;
    }

    public void setEscalafon(String escalafon) {
        this.escalafon = escalafon;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRutJefe() {
        return rutJefe;
    }

    public void setRutJefe(String rutJefe) {
        this.rutJefe = rutJefe;
    }

    public String getNombreJefe() {
        return nombreJefe;
    }

    public void setNombreJefe(String nombreJefe) {
        this.nombreJefe = nombreJefe;
    }

    public String getRutDirector() {
        return rutDirector;
    }

    public void setRutDirector(String rutDirector) {
        this.rutDirector = rutDirector;
    }

    public String getNombreDirector() {
        return nombreDirector;
    }

    public void setNombreDirector(String nombreDirector) {
        this.nombreDirector = nombreDirector;
    }

    public String getNombreSolicitud() {
        return nombreSolicitud;
    }

    public void setNombreSolicitud(String nombreSolicitud) {
        this.nombreSolicitud = nombreSolicitud;
    }

    public static class Builder {

        private Long idSol;
        private Long tipoSolicitud;
        private String jornada;
        private String nroIniDia;
        private String mesIni;
        private String nroFinDia;
        private String mesFin;
        private String diasTomados;
        private String rut;
        private String vrut;
        private String paterno;
        private String materno;
        private String nombres;
        private String depto;
        private String escalafon;
        private String grado;
        private String telefono;
        private String rutJefe;
        private String nombreJefe;
        private String rutDirector;
        private String nombreDirector;
        private String nombreSolicitud;

        public Builder idSol(Long idSol) {
            this.idSol = idSol;
            return this;
        }

        public Builder tipoSolicitud(Long tipoSolicitud) {
            this.tipoSolicitud = tipoSolicitud;
            return this;
        }

        public Builder jornada(String jornada) {
            this.jornada = jornada;
            return this;
        }

        public Builder nroIniDia(String nroIniDia) {
            this.nroIniDia = nroIniDia;
            return this;
        }

        public Builder mesIni(String mesIni) {
            this.mesIni = mesIni;
            return this;
        }

        public Builder nroFinDia(String nroFinDia) {
            this.nroFinDia = nroFinDia;
            return this;
        }

        public Builder mesFin(String mesFin) {
            this.mesFin = mesFin;
            return this;
        }

        public Builder diasTomados(String diasTomados) {
            this.diasTomados = diasTomados;
            return this;
        }

        public Builder rut(String rut) {
            this.rut = rut;
            return this;
        }

        public Builder vrut(String vrut) {
            this.vrut = vrut;
            return this;
        }

        public Builder paterno(String paterno) {
            this.paterno = paterno;
            return this;
        }

        public Builder materno(String materno) {
            this.materno = materno;
            return this;
        }

        public Builder nombres(String nombres) {
            this.nombres = nombres;
            return this;
        }

        public Builder depto(String depto) {
            this.depto = depto;
            return this;
        }

        public Builder escalafon(String escalafon) {
            this.escalafon = escalafon;
            return this;
        }

        public Builder grado(String grado) {
            this.grado = grado;
            return this;
        }

        public Builder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        public Builder rutJefe(String rutJefe) {
            this.rutJefe = rutJefe;
            return this;
        }

        public Builder nombreJefe(String nombreJefe) {
            this.nombreJefe = nombreJefe;
            return this;
        }

        public Builder rutDirector(String rutDirector) {
            this.rutDirector = rutDirector;
            return this;
        }

        public Builder nombreDirector(String nombreDirector) {
            this.nombreDirector = nombreDirector;
            return this;
        }

        public Builder nombreSolicitud(String nombreSolicitud) {
            this.nombreSolicitud = nombreSolicitud;
            return this;
        }

        public PdfDto build() {
            return new PdfDto(this);
        }

    }

}