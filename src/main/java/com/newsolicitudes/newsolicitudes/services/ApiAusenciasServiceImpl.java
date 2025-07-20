package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.AusenciasResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiAusenciasService;

import reactor.core.publisher.Mono;

@Service
public class ApiAusenciasServiceImpl implements ApiAusenciasService {

    private final WebClient webClient;

        public ApiAusenciasServiceImpl(WebClient.Builder webClientBuilder,
                        ApiProperties apiProperties
                        ) {
                this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
        }


    @Override
    public List<AusenciasResponse> getAusenciasByRutAndFechas(Integer rut, Integer ident, LocalDate fechaInicio,
            LocalDate fechaFin) {
        return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/ausencias")
                                                .queryParam("rut", rut)
                                                .queryParam("ident", ident)
                                                .queryParam("fechaInicio", fechaInicio)
                                                .queryParam("fechaFin", fechaFin)
                                                .build())
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<List<AusenciasResponse>>() {})
                                .onErrorResume(Exception.class, e -> Mono.empty())
                                .block();
    }

}
