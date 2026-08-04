package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.LicenciasDeptos;
import com.newsolicitudes.newsolicitudes.services.apilcenciasdeptos.LicenciasEntreFechasDepto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;




@RestController
@RequestMapping("/solicitudes/licencias")
@CrossOrigin(origins = "http://localhost:5173")
public class LicenciasController {

    private final LicenciasEntreFechasDepto licenciasEntreFechasDepto;

    public LicenciasController(LicenciasEntreFechasDepto licenciasEntreFechasDepto) {
        this.licenciasEntreFechasDepto = licenciasEntreFechasDepto;
    }

    @GetMapping("/departamentos")
    public ResponseEntity<List<LicenciasDeptos>> getLicenciasPorDepartamentos(
            @RequestParam List<String> deptos,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaTermino) {
        List<LicenciasDeptos> licencias = licenciasEntreFechasDepto.obtenerLicencias(deptos, fechaInicio, fechaTermino);
        return ResponseEntity.ok(licencias);
    }


}
