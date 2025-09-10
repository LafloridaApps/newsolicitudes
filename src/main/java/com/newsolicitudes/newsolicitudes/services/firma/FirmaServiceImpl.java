package com.newsolicitudes.newsolicitudes.services.firma;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.PdfDto;
import com.newsolicitudes.newsolicitudes.exceptions.DocumentException;

@Service
public class FirmaServiceImpl implements FirmaService {

    private final RestTemplate restTemplate;

    private final ApiProperties apiProperties;

    public FirmaServiceImpl(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    @Override
    public String firmarPdf(PdfDto pdfDto) {

        String apiUrl = apiProperties.getFirmaUrl();
        String url = apiProperties.getPdfUrl();

        try {

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, pdfDto, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseString = response.getBody();

                return url + responseString;
            } else {
                throw new DocumentException("No se puede firmar el docuemnto pdf");
            }

        } catch (Exception e) {
            throw new DocumentException("No se puede conectar con la Api externa");
        }

    }

}
