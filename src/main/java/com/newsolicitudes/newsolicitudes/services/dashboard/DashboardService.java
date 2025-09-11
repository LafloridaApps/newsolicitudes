package com.newsolicitudes.newsolicitudes.services.dashboard;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciaDto;

public interface DashboardService {
    List<DashboardAusenciaDto> getAusenciasPorDepartamento(Long departamentoId);
}
