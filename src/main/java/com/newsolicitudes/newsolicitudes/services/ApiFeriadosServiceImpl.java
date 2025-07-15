package com.newsolicitudes.newsolicitudes.services;


import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.ApiFeriadosResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFeriadosService;

import reactor.core.publisher.Mono;

@Service
public class ApiFeriadosServiceImpl implements ApiFeriadosService {

    private final WebClient webClient;

    public ApiFeriadosServiceImpl(WebClient.Builder webClientBuilder,
            ApiProperties apiProperties
           ) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
    }

    @Override
    public ApiFeriadosResponse obtenerFeriadosByRut(Integer rut, Integer ident) {
         return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/feriados")
                                                .queryParam("rut", rut)
                                                .queryParam("ident", ident)
                                                .build())
                                .retrieve()
                                .bodyToMono(ApiFeriadosResponse.class)
                                .onErrorResume(Exception.class, e -> Mono.empty())
                                .block();
    }

   

}
