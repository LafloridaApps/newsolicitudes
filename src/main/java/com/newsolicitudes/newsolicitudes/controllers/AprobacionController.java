package com.newsolicitudes.newsolicitudes.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.AprobacionRequest;
import com.newsolicitudes.newsolicitudes.service.aprobacioneslist.AprobacionListService;
import com.newsolicitudes.newsolicitudes.services.aprobacion.AprobacionService;

@RestController
@RequestMapping("/api/aprobaciones")
@CrossOrigin(origins = "http://localhost:5173")
public class AprobacionController {

    private final AprobacionService aprobacionService;

    private final AprobacionListService aprobacionListService;

    public AprobacionController(AprobacionService aprobacionService, AprobacionListService aprobacionListService) {
        this.aprobacionService = aprobacionService;
        this.aprobacionListService = aprobacionListService;
    }

    @PostMapping
    public ResponseEntity<Object> createAprobacion(@RequestBody AprobacionRequest request) {

        aprobacionService.aprobarSolicitud(request);
        Map<String, String> response = Map.of("message", "Solicitud aprobada con éxito");
        return ResponseEntity.ok(response);

    }

    @GetMapping("/list")
    public ResponseEntity<Object> getAprobacionesBetweenDate(@RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        List<AprobacionList> aprobaciones = aprobacionListService.getAprobacionList(fechaInicio, fechaFin);
        if (aprobaciones.isEmpty()) {
            Map<String, String> response = Map.of("message", "No se encontraron aprobaciones");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(aprobaciones);

    }

}
