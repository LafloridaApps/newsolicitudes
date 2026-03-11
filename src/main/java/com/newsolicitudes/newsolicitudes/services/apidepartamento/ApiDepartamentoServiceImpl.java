package com.newsolicitudes.newsolicitudes.services.apidepartamento;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.RecordDepartamentoRequest;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import com.newsolicitudes.newsolicitudes.dto.CargoFunc;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoJerarquiaDTO;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;

import reactor.core.publisher.Mono;

@Service
public class ApiDepartamentoServiceImpl implements ApiDepartamentoService {

        private final WebClient webClient;
        private final SubroganciaRepository subroganciaRepository;
        private static final Logger logger = LoggerFactory.getLogger(ApiDepartamentoServiceImpl.class);

        public ApiDepartamentoServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties,
                        SubroganciaRepository subroganciaRepository) {
                this.webClient = webClientBuilder.baseUrl(apiProperties.getDepartamentoUrl()).build();
                this.subroganciaRepository = subroganciaRepository;
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

                CargoFunc cargoFunc = webClient.get()
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

                if (cargoFunc != null
                                && subroganciaRepository
                                                .existsBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                                                rut,
                                                                FechaUtils.fechaActual(), // para fechaInicio <= hoy
                                                                FechaUtils.fechaActual() // para fechaTermino >= hoy
                                                )) {

                        cargoFunc.setEsJefe(true);
                }
                return cargoFunc;

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

        @Override
        public List<DepartamentoList> getDepartamentosList() {
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/list").build())
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                                .bodyToMono(new ParameterizedTypeReference<List<DepartamentoList>>() {
                                })
                                .onErrorResume(e -> Mono.empty())
                                .block();
        }

        @Override
        public DepartamentoJerarquiaDTO getJerarquiaPorId(Long id) {
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/jerarquia/{id}").build(id))
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError,
                                                response -> response.createException().flatMap(Mono::error))
                                .bodyToMono(DepartamentoJerarquiaDTO.class)
                                .onErrorResume(e -> Mono.empty())
                                .block();
        }

        @Override
        public Object updateJefeDepartamento(Long idDepto, Integer rut) {

                Object response = webClient.put()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/{idDepto}")
                                                .queryParam("rut", rut)
                                                .build(idDepto))
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError,
                                                response1 -> response1.createException().flatMap(Mono::error))
                                .bodyToMono(Object.class)
                                .onErrorResume(e -> {
                                        logger.error("Error al llamar a la API externa para actualizar jefe de departamento",
                                                        e);
                                        return Mono.empty();
                                })
                                .block();

                logger.info("Respuesta de la API externa al actualizar jefe de departamento: {}", response);
                return response;
        }

        @Override
        public Object updateNombreDepartamento(Long idDepto, String nombre) {
                Object response = webClient.put()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/{idDepto}/nombre")
                                                .queryParam("nombre", nombre)
                                                .build(idDepto))
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError,
                                                response1 -> response1.createException().flatMap(Mono::error))
                                .bodyToMono(Object.class)
                                .onErrorResume(e -> {
                                        logger.error("Error al llamar a la API externa para actualizar nombre de departamento",
                                                        e);
                                        return Mono.empty();
                                })
                                .block();
                logger.info("Respuesta de la API externa al actualizar nombre de departamento: {}", response);
                return response;
        }

        @Override
        public Object updateCodigoExternoDepartamento(Long idDepto, String codigoExterno) {

                Object response = webClient.put()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/{idDepto}/codigoEx")
                                                .queryParam("codigoEx", codigoExterno)
                                                .build(idDepto))
                                .retrieve()
                                .onStatus(HttpStatusCode::isError,
                                                response1 -> response1.bodyToMono(
                                                                new ParameterizedTypeReference<Map<String, Object>>() {
                                                                })
                                                                .flatMap(error -> Mono.error(new RuntimeException(
                                                                                (String) error.get("mensaje")))))
                                .bodyToMono(Object.class)
                                .onErrorResume(e -> {
                                        logger.error("Error al llamar a la API externa para actualizar codigo externo de departamento",
                                                        e);
                                        return Mono.error(e);
                                })
                                .block();
                logger.info("Respuesta de la API externa al actualizar codigo externo de departamento: {}", response);
                return response;
        }

        @Override
        public Object delteCodigoExternoByIdDepto(Long idDepto) {
                Object response = webClient.delete()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/{idDepto}/codex-del")
                                                .build(idDepto))
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError,
                                                response1 -> response1.createException().flatMap(Mono::error))
                                .bodyToMono(Object.class)
                                .onErrorResume(e -> {
                                        logger.error("Error al llamar a la API externa para eliminar codigo externo de departamento",
                                                        e);
                                        return Mono.error(e);
                                })
                                .block();
                logger.info("Respuesta de la API externa al eliminar codigo externo de departamento: {}", response);
                return response;
        }

        @Override
        public Object agregarDepartamento(RecordDepartamentoRequest request) {
                Object response = webClient.post()
                                .uri(uriBuilder -> uriBuilder.path("/api/departamentos/add")
                                                .build())
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError,
                                                response1 -> response1.createException().flatMap(Mono::error))
                                .bodyToMono(Object.class)
                                .onErrorResume(e -> {
                                        logger.error("Error al llamar a la API externa para agregar el departamento",
                                                        e);
                                        return Mono.error(e);
                                })
                                .block();
                logger.info("Respuesta de la API externa al agregar  departamento: {}", response);
                return response;
        }

}
