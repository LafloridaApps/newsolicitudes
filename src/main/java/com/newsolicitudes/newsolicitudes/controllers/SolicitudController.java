package com.newsolicitudes.newsolicitudes.controllers;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.services.interfaces.SolicitudService;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/crear")
    public ResponseEntity<Object> crearSolicitud(@RequestBody SolicitudRequest solicitud) {

        
            return ResponseEntity.ok(solicitudService.createSolicitud(solicitud));
       
    }

    @GetMapping("/existe")
    public ResponseEntity<Object> existeSolicitud(@RequestParam Integer rut, LocalDate fechaInicio, String tipo) {
        
            return ResponseEntity.ok(solicitudService.existeSolicitudByFechaAndTipo(rut, fechaInicio,tipo));
        
    }

}
