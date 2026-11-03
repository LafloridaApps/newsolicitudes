package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.*;
import com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudTercerosService;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SearchViewFuncionarioResponse;

@RestController
@RequestMapping("/solicitudes/terceros")
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudTercerosController {
    private final SolicitudTercerosService service;
    public SolicitudTercerosController(SolicitudTercerosService service) { this.service = service; }

    @GetMapping("/funcionario")
    public FuncionarioResponseApi obtener(@RequestParam String rutGestor, @RequestParam Integer rut) {
        return service.validarSolicitante(rutGestor, rut);
    }

    @GetMapping("/buscar-nombre")
    public SearchViewFuncionarioResponse buscar(@RequestParam String rutGestor, @RequestParam String pattern,
            @RequestParam int pageNumber) {
        return service.buscarPorNombre(rutGestor, pattern, pageNumber);
    }
}
