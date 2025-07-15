package com.newsolicitudes.newsolicitudes.dto;

public class EntradaRequest {

    private Long idDerivacion;
    private Integer rutFuncionario;

    public Integer getRutFuncionario() {
        return rutFuncionario;
    }

    public void setRutFuncionario(Integer rutFuncionario) {
        this.rutFuncionario = rutFuncionario;
    }

    public Long getIdDerivacion() {
        return idDerivacion;
    }

    public void setIdDerivacion(Long idDerivacion) {
        this.idDerivacion = idDerivacion;
    }

}
