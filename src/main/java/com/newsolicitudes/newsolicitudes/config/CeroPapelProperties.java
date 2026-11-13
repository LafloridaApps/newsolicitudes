package com.newsolicitudes.newsolicitudes.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ceropapel")
public class CeroPapelProperties {
    private boolean habilitada = true;
    private String encryptionKey = "";
    private String hostsPermitidos = "laflorida.demo.ceropapel.cl";

    public boolean isHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean value) {
        habilitada = value;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String value) {
        encryptionKey = value;
    }

    public String getHostsPermitidos() {
        return hostsPermitidos;
    }

    public void setHostsPermitidos(String value) {
        hostsPermitidos = value;
    }

    private String baseUrl = "";
    private String usuario = "app_test";
    private String password = "";
    private Map<Long, Long> perfilesPorDepartamento = new HashMap<>();

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        password = value;
    }

    public Map<Long, Long> getPerfilesPorDepartamento() {
        return perfilesPorDepartamento;
    }

    public void setPerfilesPorDepartamento(Map<Long, Long> value) {
        perfilesPorDepartamento = value;
    }
}
