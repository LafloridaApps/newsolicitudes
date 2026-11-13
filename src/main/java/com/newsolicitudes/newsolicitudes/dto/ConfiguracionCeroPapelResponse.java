package com.newsolicitudes.newsolicitudes.dto;

public record ConfiguracionCeroPapelResponse(
        boolean habilitada, String url, String usuario, boolean claveConfigurada, Long version) {}
