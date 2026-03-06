package com.newsolicitudes.newsolicitudes.services.mail;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;

@Service
public class ApiMailServiceImpl implements ApiMailService {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(ApiMailServiceImpl.class);

 public ApiMailServiceImpl(WebClient.Builder webClientBuilder,
            ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getMailUrl()).build();
    }
    @Override
    public void enviarMail(String to, String subject, String templateName, java.util.Map<String, Object> body) {
       
        webClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path("/api/email/send")
                    .queryParam("to", to)
                    .queryParam("subject", subject)
                    .queryParam("templateName", templateName)
                    .build())
            .bodyValue(body) // 
            .retrieve()
            .toBodilessEntity() 
            .doOnError(e -> logger.error("Fallo al enviar correo a {}: {}", to, e.getMessage()))
            .block();
    }

}
