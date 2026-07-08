package com.newsolicitudes.newsolicitudes.controllers;

import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.services.busqueda.BusquedaSolicitudService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/solicitudes/busqueda")
@CrossOrigin(origins = "http://localhost:5173")
public class BusquedaSolicitudController {

    private final BusquedaSolicitudService busquedaSolicitudService;

    public BusquedaSolicitudController(BusquedaSolicitudService busquedaSolicitudService) {
        this.busquedaSolicitudService = busquedaSolicitudService;
    }

    @GetMapping
    public ResponseEntity<PageSolicitudesResponse> buscarSolicitudes(
            @RequestParam Long codDepto,
            @RequestParam(required = false) String nombreSolicitante,
            @RequestParam(required = false) String rutSolicitante,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaTermino,
            @RequestParam(defaultValue = "0") int pageNumber) {
        PageSolicitudesResponse response = busquedaSolicitudService.buscarSolicitudes(
                codDepto, nombreSolicitante, rutSolicitante, fechaInicio, fechaTermino, pageNumber);
        return ResponseEntity.ok(response);
    }
}
