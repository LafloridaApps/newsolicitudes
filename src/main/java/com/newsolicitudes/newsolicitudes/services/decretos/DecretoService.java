package com.newsolicitudes.newsolicitudes.services.decretos;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DecretoDeleteRequest;
import com.newsolicitudes.newsolicitudes.dto.DecretoDto;

public interface DecretoService {

    List<AprobacionList> decretar(Set<Long> ids, Integer rut);

    void revertirDecreto(DecretoDeleteRequest request); // Modified to accept DecretoDeleteRequest

    List<DecretoDto> findDecretosByFecha(LocalDate fechaInicio, LocalDate fechaFin); // New method

}
