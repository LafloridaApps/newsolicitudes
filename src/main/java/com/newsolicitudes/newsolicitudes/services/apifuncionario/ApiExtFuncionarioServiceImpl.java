package com.newsolicitudes.newsolicitudes.services.apifuncionario;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;

import reactor.core.publisher.Mono;

@Service
public class ApiExtFuncionarioServiceImpl implements ApiExtFuncionarioService {

    private final WebClient webClient;

    public ApiExtFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioUrl()).build();
    }

    @Override
    public FuncionarioResponseApi obtenerDetalleColaborador(Integer rut) {


        return webClient.get()
                .uri(uriBuilder -> {
                    String uri = uriBuilder
                            .path("/api/funcionario")
                            .queryParam("rut", rut)
                            .build()
                            .toString();
                    return URI.create(uri);
                })
                .header("Accept", "application/json")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> Mono.empty())
                .bodyToMono(FuncionarioResponseApi.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();

    }

    @Override
    public SearchFuncionarioResponse buscarFuncionarioByNombre(String pattern, int pageNmber) {
        return webClient.get()
                .uri(uriBuilder -> {
                    String uri = uriBuilder
                            .path("/api/funcionario/search")
                            .queryParam("pattern", pattern)
                            .queryParam("pageNumber", pageNmber)
                            .build()
                            .toString();
                    return URI.create(uri);
                })
                .header("Accept", "application/json")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> Mono.empty())
                .bodyToMono(SearchFuncionarioResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

}
