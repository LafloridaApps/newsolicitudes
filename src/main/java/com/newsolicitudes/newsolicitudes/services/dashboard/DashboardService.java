package com.newsolicitudes.newsolicitudes.services.dashboard;

import java.time.LocalDate;
import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciaDto;

public interface DashboardService {
    List<DashboardAusenciaDto> getAusenciasPorDepartamento(Long departamentoId, LocalDate fecha);
}
