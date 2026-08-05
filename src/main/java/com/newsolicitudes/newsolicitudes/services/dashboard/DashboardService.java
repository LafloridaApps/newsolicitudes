package com.newsolicitudes.newsolicitudes.services.dashboard;

import java.time.LocalDate;

import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciasResponseDto;
import com.newsolicitudes.newsolicitudes.dto.DashboardResponseDto;

public interface DashboardService {

    DashboardAusenciasResponseDto getAusenciasPorDepartamento(Long departamentoId, LocalDate fecha);
    
    /**
     * Obtiene las métricas necesarias para el dashboard de informes, restringido a la Dirección del usuario.
     *
     * @param codDeptoUsuario El código del departamento del usuario consultante.
     * @param anio            El año sobre el cual calcular las métricas.
     * @param codDeptoFiltro  (Opcional) Código del departamento específico a filtrar. Nulo si se requieren todos.
     * @return Estructura de datos completa requerida por el frontend.
     */
    DashboardResponseDto obtenerResumenDashboard(Long codDeptoUsuario, Integer anio, Long codDeptoFiltro);
}