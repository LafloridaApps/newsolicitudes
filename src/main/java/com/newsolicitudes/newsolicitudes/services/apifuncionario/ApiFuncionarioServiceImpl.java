package com.newsolicitudes.newsolicitudes.services.apifuncionario;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;

import reactor.core.publisher.Mono;

@Service
public class ApiFuncionarioServiceImpl implements ApiFuncionarioService {

    private final WebClient webClient;

    public ApiFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getNewfuncionarioUrl()).build();
    }

    @Override
    public FuncionarioResponse obtenerDetalleColaborador(Integer rut) {

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
                .bodyToMono(FuncionarioResponse.class)
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
