package com.newsolicitudes.newsolicitudes.services.apiextdepartamentos;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoDto;

import reactor.core.publisher.Mono;

@Service
public class DepartamentoExternoServiceImpl implements DepartamentoExternoService {

    private final WebClient webClient;

    public DepartamentoExternoServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getExternoUrl()).build();
    }

    @Override
    public List<DepartamentoDto> getDepartamentosExternosList() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/departamentos").build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(new ParameterizedTypeReference<List<DepartamentoDto>>() {
                })
                .onErrorResume(e -> Mono.empty())
                .block();
    }

}
