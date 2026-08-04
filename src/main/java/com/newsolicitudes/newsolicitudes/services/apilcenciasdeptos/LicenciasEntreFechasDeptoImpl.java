package com.newsolicitudes.newsolicitudes.services.apilcenciasdeptos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.LicenciasDeptos;

import reactor.core.publisher.Mono;

@Service
public class LicenciasEntreFechasDeptoImpl implements LicenciasEntreFechasDepto{


    private final WebClient webClient;


    public LicenciasEntreFechasDeptoImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getExternoUrl()).build();
    }

    @Override
    public List<LicenciasDeptos> obtenerLicencias(List<String> deptos, LocalDate fechaInicio, LocalDate fechaTermino) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/licencias-entre-fechas")
                        .queryParam("deptos", deptos.toArray())
                        .queryParam("fechaIni", fechaInicio.toString())
                        .queryParam("fechaFin", fechaTermino.toString())
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(new ParameterizedTypeReference<List<LicenciasDeptos>>() {
                })
                .onErrorResume(e -> Mono.empty())
                .block();
    }

}
