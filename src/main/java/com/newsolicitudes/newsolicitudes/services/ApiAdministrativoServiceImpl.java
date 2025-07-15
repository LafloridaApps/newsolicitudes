package com.newsolicitudes.newsolicitudes.services;


import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.ApiAdministrativoResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiAdmistrativoService;

import reactor.core.publisher.Mono;

@Service
public class ApiAdministrativoServiceImpl implements ApiAdmistrativoService {

        private final WebClient webClient;

        public ApiAdministrativoServiceImpl(WebClient.Builder webClientBuilder,
                        ApiProperties apiProperties
                        ) {
                this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
        }

        @Override
        public ApiAdministrativoResponse obtenerAdministrativos(Integer rut, Integer ident) {
               return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/administrativos")
                                                .queryParam("rut", rut)
                                                .queryParam("ident", ident)
                                                .build())
                                .retrieve()
                                .bodyToMono(ApiAdministrativoResponse.class)
                                .onErrorResume(Exception.class, e -> Mono.empty())
                                .block();


        }

        
}
