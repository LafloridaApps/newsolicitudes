package com.newsolicitudes.newsolicitudes.services;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiNewFuncionarioService;

import reactor.core.publisher.Mono;

@Service
public class ApiNewFuncionarioServiceImpl implements ApiNewFuncionarioService {

    private final WebClient webClient;

    public ApiNewFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getNewfuncionarioUrl()).build();
    }

    @Override
    public FuncionarioResponse obtenerDetalleColaborador(Integer rut) {
        System.out.println("Iniciando solicitud al servicio de funcionario con rut: " + rut);

        FuncionarioResponse response = webClient.get()
                .uri(uriBuilder -> {
                    String uri = uriBuilder
                            .path("/api/funcionario")
                            .queryParam("rut", rut)
                            .build()
                            .toString();
                    System.out.println("URI construida: " + uri);
                    return URI.create(uri);
                })
                .header("Accept", "application/json")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> {
                    System.out.println("Error 4xx recibido: " + responseStatus.statusCode());
                    return Mono.empty();
                })
                .bodyToMono(FuncionarioResponse.class)
                .doOnNext(funcionario -> System.out.println("Respuesta recibida: " + funcionario))
                .doOnError(e -> System.out.println("Ocurrió un error: " + e.getMessage()))
                .onErrorResume(Exception.class, e -> {
                    System.out.println("Error manejado: " + e.getMessage());
                    return Mono.empty();
                })
                .block();

        System.out.println("Resultado final del bloque: " + response);
        return response;

    }

}
