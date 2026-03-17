package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;

public class SearchSubroganciResponse {

        private String nombreJefe;
        private String subrogante;
        private String departamento;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private String estado;
        private Integer rutJefe;
        private Character vrutJefe;
        private Integer rutSubrogante;
        private Character vrutSubrogante;
        private Long idSubrogancia;

        public String getNombreJefe() {
                return nombreJefe;
        }

        public String getSubrogante() {
                return subrogante;
        }

        public String getDepartamento() {
                return departamento;
        }

        public LocalDate getFechaInicio() {
                return fechaInicio;
        }

        public LocalDate getFechaFin() {
                return fechaFin;
        }

        public String getEstado() {
                return estado;
        }

        public Integer getRutJefe() {
                return rutJefe;
        }

        public Character getVrutJefe() {
                return vrutJefe;
        }

        public Integer getRutSubrogante() {
                return rutSubrogante;
        }

        public Character getVrutSubrogante() {
                return vrutSubrogante;
        }

        public Long getIdSubrogancia(){
                return idSubrogancia;
        }

        private SearchSubroganciResponse(Builder builder) {
                this.nombreJefe = builder.nombreJefe;
                this.subrogante = builder.subrogante;
                this.departamento = builder.departamento;
                this.fechaInicio = builder.fechaInicio;
                this.fechaFin = builder.fechaFin;
                this.estado = builder.estado;
                this.rutJefe = builder.rutJefe;
                this.vrutJefe = builder.vrutJefe;
                this.rutSubrogante = builder.rutSubrogante;
                this.vrutSubrogante = builder.vrutSubrogante;
                this.idSubrogancia = builder.idSubrogancia;

        }

        public static class Builder {

                private String nombreJefe;
                private String subrogante;
                private String departamento;
                private LocalDate fechaInicio;
                private LocalDate fechaFin;
                private String estado;
                private Integer rutJefe;
                private Character vrutJefe;
                private Integer rutSubrogante;
                private Character vrutSubrogante;
                private Long idSubrogancia;

                public Builder nombreJefe(String nombreJefe) {
                        this.nombreJefe = nombreJefe;
                        return this;
                }

                public Builder subrogante(String subrogante) {
                        this.subrogante = subrogante;
                        return this;
                }

                public Builder departamento(String departamento) {
                        this.departamento = departamento;
                        return this;
                }

                public Builder fechaInicio(LocalDate fechaInicio) {
                        this.fechaInicio = fechaInicio;
                        return this;
                }

                public Builder fechaFin(LocalDate fechaFin) {
                        this.fechaFin = fechaFin;
                        return this;
                }

                public Builder estado(String estado) {
                        this.estado = estado;
                        return this;
                }

                public Builder rutJefe(Integer rutJefe) {
                        this.rutJefe = rutJefe;
                        return this;
                }

                public Builder vrutJefe(Character vrutJefe) {
                        this.vrutJefe = vrutJefe;
                        return this;
                }

                public Builder rutSobrogante(Integer rutSubrogante) {
                        this.rutSubrogante = rutSubrogante;
                        return this;
                }

                public Builder vrutSubrogante(Character vrutSubrogante) {
                        this.vrutSubrogante = vrutSubrogante;
                        return this;
                }

                public Builder idSubrogancia(Long idSubrogancia) {
                        this.idSubrogancia = idSubrogancia;
                        return this;
                }


                public SearchSubroganciResponse build() {
                        return new SearchSubroganciResponse(this);
                }

        }
}
