package com.newsolicitudes.newsolicitudes.services;

import java.net.URI;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.CargoFunc;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;

import reactor.core.publisher.Mono;

@Service
public class ApiDepartamentoServiceImpl implements ApiDepartamentoService {

    private final WebClient webClient;

    public ApiDepartamentoServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getDepartamentoUrl()).build();
    }

    @Override
    public DepartamentoResponse obtenerDepartamento(Long idDepto) {
        return webClient.get()
                .uri(uriBuilder -> {
                    String uri = uriBuilder
                            .path("/api/departamentos")
                            .queryParam("id", idDepto)
                            .build()
                            .toString();
                    return URI.create(uri);
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> Mono.empty())
                .bodyToMono(DepartamentoResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();

    }

    @Override
    public CargoFunc obtenerJefeFunc(Long idDepto, Integer rut) {
        return webClient.get()
                .uri(uriBuilder -> {
                    String uri = uriBuilder
                            .path("/api/departamentos/esjefe")
                            .queryParam("depto", idDepto)
                            .queryParam("rut", rut)
                            .build()
                            .toString();
                    return URI.create(uri);
                })
                .header("Accept", "application/json")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, responseStatus -> Mono.empty())
                .bodyToMono(CargoFunc.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

    @Override
    public List<DepartamentoResponse> obtenerFamiliaDepto(Long idDepto) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/familia/{id}").build(idDepto))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(new ParameterizedTypeReference<List<DepartamentoResponse>>() {
                })
                .onErrorResume(e -> Mono.empty())
                .block();
    }

}
