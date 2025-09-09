package com.newsolicitudes.newsolicitudes.dto;

import java.time.LocalDate;
import java.util.List;

public class DecretoConSolicitudesDTO {

    private Long idDecreto;
    private LocalDate fechaDecreto;
    private List<SolicitudInfoDTO> solicitudes;

    public DecretoConSolicitudesDTO(Long idDecreto, LocalDate fechaDecreto, List<SolicitudInfoDTO> solicitudes) {
        this.idDecreto = idDecreto;
        this.fechaDecreto = fechaDecreto;
        this.solicitudes = solicitudes;
    }

    // Getters and Setters
    public Long getIdDecreto() {
        return idDecreto;
    }

    public void setIdDecreto(Long idDecreto) {
        this.idDecreto = idDecreto;
    }

    public LocalDate getFechaDecreto() {
        return fechaDecreto;
    }

    public void setFechaDecreto(LocalDate fechaDecreto) {
        this.fechaDecreto = fechaDecreto;
    }

    public List<SolicitudInfoDTO> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<SolicitudInfoDTO> solicitudes) {
        this.solicitudes = solicitudes;
    }
}
