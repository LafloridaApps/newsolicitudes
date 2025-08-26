package com.newsolicitudes.newsolicitudes.services.templates;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.newsolicitudes.newsolicitudes.entities.DocTemplates;
import com.newsolicitudes.newsolicitudes.repositories.DocTemplatesRepository;
import com.newsolicitudes.newsolicitudes.services.files.ArchivoService;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final ArchivoService archivoService;

    private final DocTemplatesRepository docTemplatesRepository;

    public TemplateServiceImpl(ArchivoService archivoService, DocTemplatesRepository docTemplatesRepository) {
        this.archivoService = archivoService;
        this.docTemplatesRepository = docTemplatesRepository;
    }

    @Override
    public void upLoadTemplate(MultipartFile file, String nombrePlantilla) throws IOException {

        String nombreGuardaddo = archivoService.guardarArchivo(file);

        DocTemplates docTemplates = new DocTemplates();
        docTemplates.setNombre(nombrePlantilla);
        docTemplates.setDocFile(nombreGuardaddo);

        docTemplatesRepository.save(docTemplates);

    }

}
