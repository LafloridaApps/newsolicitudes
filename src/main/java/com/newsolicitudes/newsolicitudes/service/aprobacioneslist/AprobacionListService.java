package com.newsolicitudes.newsolicitudes.service.aprobacioneslist;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.AprobacionListPage;

public interface AprobacionListService {

    AprobacionListPage getAprobacionList(LocalDate fechaInicio, LocalDate fechaTermino, int pageNumber);

}
