package com.newsolicitudes.newsolicitudes.services.templates;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {

    void upLoadTemplate(MultipartFile  fila, String nombrePlantilla) throws IOException;

}
