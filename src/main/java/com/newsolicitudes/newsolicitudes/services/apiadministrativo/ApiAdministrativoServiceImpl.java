package com.newsolicitudes.newsolicitudes.services.apiadministrativo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.ApiAdministrativoResponse;

import reactor.core.publisher.Mono;

@Service
public class ApiAdministrativoServiceImpl implements ApiAdmistrativoService {

        private static final Logger logger = LoggerFactory.getLogger(ApiAdministrativoServiceImpl.class);

        private final WebClient webClient;
        private final String baseUrl;

        public ApiAdministrativoServiceImpl(WebClient.Builder webClientBuilder,
                        ApiProperties apiProperties) {
                this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
                this.baseUrl = apiProperties.getFuncionarioUrl();
        }

        @Override
        public ApiAdministrativoResponse obtenerAdministrativos(Integer rut, Integer ident) {
                String url = baseUrl + "/api/funcionario/administrativos?rut=" + rut + "&ident=" + ident;
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/administrativos")
                                                .queryParam("rut", rut)
                                                .queryParam("ident", ident)
                                                .build())
                                .retrieve()
                                .bodyToMono(ApiAdministrativoResponse.class)
                                .doOnError(e -> logger.error(
                                                "Error obteniendo administrativos para rut={}, ident={}, url={}: {}",
                                                rut, ident, url, e.getMessage()))
                                .onErrorResume(Exception.class, e -> Mono.empty())
                                .block();

        }

}
