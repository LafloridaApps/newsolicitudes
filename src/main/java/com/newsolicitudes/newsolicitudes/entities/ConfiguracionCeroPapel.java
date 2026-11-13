package com.newsolicitudes.newsolicitudes.entities;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "configuracion_ceropapel", check = @CheckConstraint(name = "chk_configuracion_ceropapel_unica", constraint = "id = 1"))
public class ConfiguracionCeroPapel {
    @Id
    private Long id;
    @Column(nullable = false)
    private boolean habilitada;
    @Column(name = "base_url", nullable = false, length = 2048)
    private String baseUrl;
    @Column(nullable = false, length = 100)
    private String usuario;
    @Column(name = "clave_cifrada", nullable = false, length = 4096)
    private byte[] claveCifrada;
    @Version
    private Long version;
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public ConfiguracionCeroPapel() {
        // JPA requiere un constructor sin argumentos para materializar la entidad.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public boolean isHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean value) {
        habilitada = value;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String value) {
        baseUrl = value;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String value) {
        usuario = value;
    }

    public byte[] getClaveCifrada() {
        return claveCifrada == null ? null : claveCifrada.clone();
    }

    public void setClaveCifrada(byte[] value) {
        claveCifrada = value == null ? null : value.clone();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long value) {
        version = value;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime value) {
        actualizadoEn = value;
    }
}
