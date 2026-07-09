package com.newsolicitudes.newsolicitudes.services.decretos;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DecretoConSolicitudesDTO;
import com.newsolicitudes.newsolicitudes.dto.DecretoDeleteRequest;
import com.newsolicitudes.newsolicitudes.dto.DecretoDto;

public interface DecretoService {

    List<AprobacionList> decretar(Set<Long> ids, Integer rut, String template);

    void revertirDecreto(DecretoDeleteRequest request);

    List<DecretoDto> findDecretosByFecha(LocalDate fechaInicio, LocalDate fechaFin);

    byte[] getDecretoDocumento(Long id);

    String getDecretoDocumentoPath(Long id);

    byte[] getDecretoExcelDocumento(Long id);

    Page<DecretoConSolicitudesDTO> searchDecretos(Long id, LocalDate fechaDesde, LocalDate fechaHasta, Integer rut, Long idSolicitud, String nombreFuncionario, Pageable pageable);

    String generarReporteSolicitudes(Long idDecreto, String template);

}
