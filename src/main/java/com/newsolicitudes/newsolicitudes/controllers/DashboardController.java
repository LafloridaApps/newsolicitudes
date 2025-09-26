package com.newsolicitudes.newsolicitudes.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciaDto;
import com.newsolicitudes.newsolicitudes.services.dashboard.DashboardService;

@RestController
@RequestMapping("/solicitudes/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/ausencias/departamento/{departamentoId}")
    public List<DashboardAusenciaDto> getAusenciasPorDepartamento(@PathVariable Long departamentoId) {
        return dashboardService.getAusenciasPorDepartamento(departamentoId);
    }
}
