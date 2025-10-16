package com.newsolicitudes.newsolicitudes.services.apiconsultafirma;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.FirmaFuncionarioDto;

import reactor.core.publisher.Mono;

@Service
public class ConsultaFirmaServiceImpl implements ConsultaFirmaService {

    private final WebClient webClient;

    public ConsultaFirmaServiceImpl(WebClient.Builder webClientBuilder,
            ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getConsultafirmaUrl()).build();
    }

    @Override
    public FirmaFuncionarioDto consultafirmaUrl(Integer rut) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/firma/{rut}")
                        .build(rut)) 
                .retrieve()
                .bodyToMono(FirmaFuncionarioDto.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();

    }
}