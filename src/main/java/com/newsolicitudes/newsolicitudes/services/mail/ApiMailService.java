package com.newsolicitudes.newsolicitudes.services.mail;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;

@Service
public class ApiMailService implements APiMailService {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(ApiMailService.class);

    public ApiMailService(WebClient.Builder webClientBuilder,
            ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getMailUrl()).build();
    }

    @Override
    public void enviarMail(String to, String subject, String templateName, Map<String, Object> body) {

        webClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path("/api/email/send")
                    .queryParam("to", to)
                    .queryParam("subject", subject)
                    .queryParam("templateName", templateName)
                    .build())
            .bodyValue(body) // 👈 envía el Map como JSON en el cuerpo del POST
            .retrieve()
            .toBodilessEntity() // 👈 no espera respuesta con contenido
            .doOnError(e -> logger.error("Fallo al enviar correo a {}: {}", to, e.getMessage()))
            .block();

    }

}
