package com.newsolicitudes.newsolicitudes.dto;

public class AprobacionRequest {

    private Long idDerivacion;
    private Integer aprobadoPor;

    public Long getIdDerivacion() {
        return idDerivacion;
    }

    public void setIdDerivacion(Long idDerivacion) {
        this.idDerivacion = idDerivacion;
    }

    public Integer getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(Integer aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

}
