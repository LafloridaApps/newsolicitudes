package com.newsolicitudes.newsolicitudes.dto;

public class Funcionario {
    private final Integer rut;
    private final String vrut;
    private final String nombre;
    private final String apellidoPaterno;
    private final String apellidoMaterno;
    private final String email;
    private final String departamento;
    private final Long codDepto;
    private final String nombreJefe;
    private final Long codDeptoJefe;
    private final String foto;
    private final Integer ident;
    private final String tipoContrato;

    private Funcionario(FuncionarioBuilder builder) {
        this.rut = builder.rut;
        this.vrut = builder.vrut;
        this.nombre = builder.nombre;
        this.apellidoPaterno = builder.apellidoPaterno;
        this.apellidoMaterno = builder.apellidoMaterno;
        this.email = builder.email;
        this.departamento = builder.departamento;
        this.codDepto = builder.codDepto;
        this.nombreJefe = builder.nombreJefe;
        this.codDeptoJefe = builder.codDeptoJefe;
        this.foto = builder.foto;
        this.ident = builder.ident;
        this.tipoContrato = builder.tipoContrato;
    }

    public static class FuncionarioBuilder {
        private Integer rut;
        private String vrut;
        private String nombre;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String email;
        private String departamento;
        private Long codDepto;
        private String nombreJefe;
        private Long codDeptoJefe;
        private String foto;
        private Integer ident;
        private String tipoContrato;

        public FuncionarioBuilder rut(Integer rut) {
            this.rut = rut;
            return this;
        }

        public FuncionarioBuilder vrut(String vrut) {
            this.vrut = vrut;
            return this;
        }

        public FuncionarioBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public FuncionarioBuilder apellidoPaterno(String apellidoPaterno) {
            this.apellidoPaterno = apellidoPaterno;
            return this;
        }

        public FuncionarioBuilder apellidoMaterno(String apellidoMaterno) {
            this.apellidoMaterno = apellidoMaterno;
            return this;
        }

        public FuncionarioBuilder email(String email) {
            this.email = email;
            return this;
        }

        public FuncionarioBuilder departamento(String departamento) {
            this.departamento = departamento;
            return this;
        }

        public FuncionarioBuilder codDepto(Long codDepto) {
            this.codDepto = codDepto;
            return this;
        }

        public FuncionarioBuilder nombreJefe(String nombreJefe) {
            this.nombreJefe = nombreJefe;
            return this;
        }

        public FuncionarioBuilder codDeptoJefe(Long codDeptoJefe) {
            this.codDeptoJefe = codDeptoJefe;
            return this;
        }

        public FuncionarioBuilder foto(String foto) {
            this.foto = foto;
            return this;
        }

        public FuncionarioBuilder ident(Integer ident) {
            this.ident = ident;
            return this;
        }

        public FuncionarioBuilder tipoContrato(String tipoContrato) {
            this.tipoContrato = tipoContrato;
            return this;
        }

        public Funcionario build() {
            return new Funcionario(this);
        }
    }

    public Integer getRut() {
        return rut;
    }

    public String getVrut() {
        return vrut;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartamento() {
        return departamento;
    }

    public Long getCodDepto() {
        return codDepto;
    }

    public String getNombreJefe() {
        return nombreJefe;
    }

    public Long getCodDeptoJefe() {
        return codDeptoJefe;
    }

    public String getFoto() {
        return foto;
    }

    public Integer getIdent() {
        return ident;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

}
