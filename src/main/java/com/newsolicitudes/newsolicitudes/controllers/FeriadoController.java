package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.interfaces.FeriadoService;

@RestController
@RequestMapping("/api/tablaferiados")
@CrossOrigin(origins = "http://localhost:5173")
public class FeriadoController {

    private final FeriadoService feriadoService;

    public FeriadoController(FeriadoService feriadoService) {
        this.feriadoService = feriadoService;
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllFeriados() {
        return ResponseEntity.ok(feriadoService.getAll());
    }

}
