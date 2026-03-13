package com.newsolicitudes.newsolicitudes.services.foto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;

import reactor.core.publisher.Mono;

@Service
public class FotoServiceImpl implements FotoService {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(FotoServiceImpl.class);

    public FotoServiceImpl(WebClient.Builder webClienteBuilder, ApiProperties apiProperties) {
        this.webClient = webClienteBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
    }

    @Override
    public String getFotoByRut(Integer rut) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/foto/{rut}")
                        .build(rut))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    logger.error("Error al llamar a la API externa para traer la foto del funcionario",
                            e);
                    return Mono.error(e);
                })
                .block();

    }

}
