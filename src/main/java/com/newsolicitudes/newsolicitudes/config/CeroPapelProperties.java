package com.newsolicitudes.newsolicitudes.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ceropapel")
public class CeroPapelProperties {
    private String baseUrl = "http://laflorida.demo.ceropapel.cl/inter/";
    private String usuario = "app_test";
    private String password = "";
    private Map<Long, Long> perfilesPorDepartamento = new HashMap<>();
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String value) { baseUrl = value; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String value) { usuario = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { password = value; }
    public Map<Long, Long> getPerfilesPorDepartamento() { return perfilesPorDepartamento; }
    public void setPerfilesPorDepartamento(Map<Long, Long> value) { perfilesPorDepartamento = value; }
}
