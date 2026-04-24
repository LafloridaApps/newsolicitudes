package com.newsolicitudes.newsolicitudes.service.asistencia;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.AsistenciaDto;

import reactor.core.publisher.Mono;

@Service
public class ApiAsistenciaServiceImpl implements ApiAsistenciaService {
    private final WebClient webClient;

    public ApiAsistenciaServiceImpl(WebClient.Builder webClientBuilder,
            ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getExternoUrl()).build();
    }

    @Override
    public List<AsistenciaDto> getAsitencia(Integer rut, Integer ident, LocalDate fechaInicio, LocalDate fechaFin) {
      return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/asistencia")
                        .queryParam("rut", rut)
                        .queryParam("ident", ident)
                        .queryParam("fechaInicio", fechaInicio)
                        .queryParam("fechaFin", fechaFin)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AsistenciaDto>>() {
                })
                .onErrorResume(Exception.class, e -> Mono.just(Collections.emptyList()))
                .block();
    }

}
