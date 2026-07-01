package com.newsolicitudes.newsolicitudes.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciaDto;
import com.newsolicitudes.newsolicitudes.dto.DashboardResponseDto;
import com.newsolicitudes.newsolicitudes.services.dashboard.DashboardService;

@RestController
@RequestMapping("/solicitudes/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/ausencias/departamento")
    public ResponseEntity<List<DashboardAusenciaDto>> getAusenciasPorDepartamento(@RequestParam Long idDepto, @RequestParam LocalDate fecha) {
        return ResponseEntity.ok(dashboardService.getAusenciasPorDepartamento(idDepto, fecha));
    }


    /**
     * Endpoint para obtener el resumen de permisos y feriados para el dashboard.
     *
     * @param codDeptoUsuario      Código del departamento del usuario.
     * @param anio                 Año a consultar.
     * @param codDeptoFiltroString (Opcional) ID de departamento para filtrar o "todos".
     * @return DashboardResponseDto con los datos estructurados para el frontend.
     */
    @GetMapping("/resumen-permisos")
    public ResponseEntity<DashboardResponseDto> obtenerResumenPermisos(
            @RequestParam("codDeptoUsuario") Long codDeptoUsuario,
            @RequestParam("anio") Integer anio,
            @RequestParam(value = "codDeptoFiltro", required = false) String codDeptoFiltroString) {
        
        Long codDeptoFiltro = null;
        if (codDeptoFiltroString != null && !codDeptoFiltroString.trim().equalsIgnoreCase("todos")) {
            codDeptoFiltro = Long.parseLong(codDeptoFiltroString);
        }
        
        DashboardResponseDto response = dashboardService.obtenerResumenDashboard(codDeptoUsuario, anio, codDeptoFiltro);
        return ResponseEntity.ok(response);
    }
}