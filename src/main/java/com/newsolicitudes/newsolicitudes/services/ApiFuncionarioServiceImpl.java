package com.newsolicitudes.newsolicitudes.services;


import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;

import reactor.core.publisher.Mono;

@Service
public class ApiFuncionarioServiceImpl implements ApiFuncionarioService {
    private final WebClient webClient;

    public ApiFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
    }

    @Override
    public ApiFuncionarioResponse obtenerDetalleColaborador(Integer rut) {
         return webClient.get()
                .uri("/api/funcionario/{rut}", rut)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(ApiFuncionarioResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();

    }

   
}
