package com.newsolicitudes.newsolicitudes.services.mail;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.exceptions.MailServiceException;

@Service
public class ApiMailService implements APiMailService {

    private final WebClient webClient;

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
            .onErrorMap(e -> new MailServiceException("Error al enviar correo al microservicio Mail", e))
            .block();

    }

}
