package com.newsolicitudes.newsolicitudes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos los endpoints
                .allowedOrigins(
                        "http://localhost",
                        "http://localhost:4200",
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "https://appd2.laflorida.cl",
                        "https://appx.laflorida.cl",
                        "https://intranet.laflorida.cl"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Métodos permitidos
                .allowedHeaders("*") // Permite cualquier cabecera
                .allowCredentials(true); // Permite el envío de credenciales (cookies, tokens de autorización)
    }
}