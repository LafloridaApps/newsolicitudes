package com.newsolicitudes.newsolicitudes.controllers;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SolicitudDetalleDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudService;

@RestController
@RequestMapping("/solicitudes/solicitudes")
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/crear")
    public ResponseEntity<Object> crearSolicitud(@RequestBody SolicitudRequest solicitud) {

        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.createSolicitud(solicitud));

    }

    @GetMapping("/existe")
    public ResponseEntity<Object> existeSolicitud(@RequestParam Integer rut, LocalDate fechaInicio, String tipo) {
        return ResponseEntity.ok(solicitudService.existeSolicitudByFechaAndTipo(rut, fechaInicio, tipo));
    }

    @GetMapping("/rut")
    public ResponseEntity<Object> getSolicitudesByRut(@RequestParam Integer rut,
            @RequestParam int page,
            @RequestParam int size

    ) {

        return ResponseEntity.ok(solicitudService.getSolicitudesByRut(rut, page, size));

    }

    @GetMapping("by-id")
    public ResponseEntity<Object> getSolicitudById(@RequestParam Long id) {
        SolicitudDetalleDto solicitudDetalleDto = solicitudService.getSolicitudDetalleById(id);
        return ResponseEntity.ok().body(solicitudDetalleDto);
    }

    @PutMapping("/{idSolicitud}")
    public ResponseEntity<Object> updateSolicitud(@PathVariable Long idSolicitud, @RequestBody com.newsolicitudes.newsolicitudes.dto.UpdateSolicitudRequest request) {
        solicitudService.updateSolicitud(idSolicitud, request);
        return ResponseEntity.ok().body("Solicitud actualizada correctamente.");
    }
}
