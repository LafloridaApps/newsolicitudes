package com.newsolicitudes.newsolicitudes.services.apifuncionario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SearchViewFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.exceptions.NotFoundException;

import reactor.core.publisher.Mono;

@Service
public class ApiExtFuncionarioServiceImpl implements ApiExtFuncionarioService {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(ApiExtFuncionarioServiceImpl.class);
    private static final String HEADER_NAME = "Accept";
    private static final String HEADER_VALUES = "application/json";

    public ApiExtFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
    }

    @Override
    public FuncionarioResponseApi obtenerDetalleColaborador(Integer rut) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/funcionario")
                        .queryParam("rut", rut)
                        .build())
                .header(HEADER_NAME, HEADER_VALUES)
                .retrieve()
                .onStatus(response -> response.value() == 404,
                        response -> Mono.error(new NotFoundException("Funcionario no encontrado con RUT: " + rut)))
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    logger.error("Error 4xx llamando a la API externa de funcionario: {}", response.statusCode());
                    return Mono
                            .error(new RuntimeException("Error del cliente en API externa: " + response.statusCode()));
                })
                .bodyToMono(FuncionarioResponseApi.class)
                .onErrorResume(e -> {
                    // Log the error but re-throw it to be handled by the global exception handler
                    logger.error("Error al procesar la respuesta de la API de funcionario", e);
                    return Mono.error(e);
                })
                .block();
    }

    @Override
    public SearchFuncionarioResponse buscarFuncionarioByNombre(String pattern, int pageNumber) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/funcionario/search")
                        .queryParam("pattern", pattern)
                        .queryParam("pageNumber", pageNumber)
                        .build())
                .header(HEADER_NAME, HEADER_VALUES)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> Mono.empty())
                .bodyToMono(SearchFuncionarioResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

    @Override
    public SearchViewFuncionarioResponse buscarFuncionarioViewNombre(String pattern, int pageNumber) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/funcionario/search-view")
                        .queryParam("pattern", pattern)
                        .queryParam("pageNumber", pageNumber)
                        .build())
                .header(HEADER_NAME, HEADER_VALUES)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> Mono.empty())
                .bodyToMono(SearchViewFuncionarioResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

}
