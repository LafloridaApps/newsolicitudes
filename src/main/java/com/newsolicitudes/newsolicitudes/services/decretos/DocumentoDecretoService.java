package com.newsolicitudes.newsolicitudes.services.decretos;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;

public interface DocumentoDecretoService {

    byte[] generarDocumento(List<AprobacionList> aprobaciones, String templateName);

}
