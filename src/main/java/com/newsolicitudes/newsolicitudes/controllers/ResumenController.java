package com.newsolicitudes.newsolicitudes.controllers;

import com.newsolicitudes.newsolicitudes.dto.ResumenJefeDepartamentoDTO;
import com.newsolicitudes.newsolicitudes.services.resumen.ResumenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/solicitudes/resumen")
@CrossOrigin(origins = "http://localhost:5173") // Ajusta según sea necesario
public class ResumenController {

    private final ResumenService resumenService;

    public ResumenController(ResumenService resumenService) {
        this.resumenService = resumenService;
    }

    @GetMapping("/jefe-departamento")
    public ResponseEntity<ResumenJefeDepartamentoDTO> getResumenJefeDepartamento(
            @RequestParam Integer rutJefe,
            @RequestParam Long idDepartamento) {
        ResumenJefeDepartamentoDTO resumen = resumenService.getResumenJefeDepartamento(rutJefe, idDepartamento);
        return ResponseEntity.ok(resumen);
    }
}