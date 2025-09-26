package com.newsolicitudes.newsolicitudes.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;

import com.newsolicitudes.newsolicitudes.config.DocProperties;
import com.newsolicitudes.newsolicitudes.dto.TemplateDto;
import com.newsolicitudes.newsolicitudes.services.templates.TemplateService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/solicitudes/doc")
@CrossOrigin(origins = "http://localhost:5173")
public class DocController {

    private final TemplateService templateService;

    private final DocProperties docProperties;

    public DocController(TemplateService templateService, DocProperties docProperties) {
        this.templateService = templateService;
        this.docProperties = docProperties;
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

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteTemplate(@RequestParam Long id) {
        templateService.deleteTemplate(id);
        Map<String, String> response = Map.of("message", "Plantilla eliminada correctamente");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<TemplateDto>> listTemplates() {
        return ResponseEntity.ok().body(templateService.listTemplates());
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        Path filePath = Paths.get(docProperties.getTemplatesPath()).resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = "application/octet-stream";
        try {
            String detectedType = Files.probeContentType(filePath);
            if (detectedType != null) {
                contentType = detectedType;
            }
        } catch (IOException ignored) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

}
