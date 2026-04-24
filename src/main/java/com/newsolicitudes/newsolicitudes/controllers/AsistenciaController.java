package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.service.asistencia.ApiAsistenciaService;
import com.newsolicitudes.newsolicitudes.dto.AsistenciaDto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/solicitudes/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class AsistenciaController {

    private final ApiAsistenciaService apiAsistenciaService;

    public AsistenciaController(ApiAsistenciaService apiAsistenciaService) {
        this.apiAsistenciaService = apiAsistenciaService;
    }

    @GetMapping("/asistencia")
    public ResponseEntity<Object> getAsistencia(
            @RequestParam Integer rut,
            @RequestParam Integer ident,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        List<AsistenciaDto> asistencia = apiAsistenciaService.getAsitencia(rut, ident, fechaInicio, fechaFin);
        return ResponseEntity.ok(asistencia);
    }

}
