package com.newsolicitudes.newsolicitudes.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PerfilDepartamentoCeroPapel {
    @Id private Long idDepto;
    private Long perfilId;
    private String ambiente;
    public PerfilDepartamentoCeroPapel() {}
    public PerfilDepartamentoCeroPapel(Long idDepto, Long perfilId, String ambiente) {
        this.idDepto = idDepto;
        this.perfilId = perfilId;
        this.ambiente = ambiente;
    }
    public Long getIdDepto() { return idDepto; }
    public Long getPerfilId() { return perfilId; }
    public String getAmbiente() { return ambiente; }
}
