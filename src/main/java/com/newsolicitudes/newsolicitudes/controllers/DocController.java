package com.newsolicitudes.newsolicitudes.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.templates.TemplateService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/agendalc/doc")
@CrossOrigin(origins = "http://localhost:5173")
public class DocController {

    private final TemplateService templateService;

    public DocController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadTemplate(
            @RequestParam String nombrePlantilla,
            @RequestParam(required = true) MultipartFile file

    ) throws IOException {

        templateService.upLoadTemplate(file, nombrePlantilla);
        Map<String, String> response = Map.of("message", "Archivo subido correctamente");
        return ResponseEntity.ok().body(response);

    }

}
