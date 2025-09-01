package com.newsolicitudes.newsolicitudes.services.templates;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.newsolicitudes.newsolicitudes.dto.TemplateDto;

import java.util.List;

public interface TemplateService {

    void upLoadTemplate(MultipartFile  fila, String nombrePlantilla) throws IOException;

    List<TemplateDto> listTemplates();

}
