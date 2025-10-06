package com.newsolicitudes.newsolicitudes.service.aprobacioneslist;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;

public interface AprobacionListService {

    List<AprobacionList> getAprobacionList(LocalDate fechaInicio, LocalDate fechaTermino);

}
