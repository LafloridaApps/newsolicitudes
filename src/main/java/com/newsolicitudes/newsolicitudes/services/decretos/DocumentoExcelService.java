package com.newsolicitudes.newsolicitudes.services.decretos;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;

public interface DocumentoExcelService {

    String generarExcel(List<AprobacionList> aprobaciones);

}
