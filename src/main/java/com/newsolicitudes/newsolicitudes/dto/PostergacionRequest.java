package com.newsolicitudes.newsolicitudes.dto;

public class PostergacionRequest {

    private Long idSolicitud;
    private String motivo;
    private Integer postergadoPor;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getPostergadoPor() {
        return postergadoPor;
    }

    public void setPostergadoPor(Integer postergadoPor) {
        this.postergadoPor = postergadoPor;
    }

}
