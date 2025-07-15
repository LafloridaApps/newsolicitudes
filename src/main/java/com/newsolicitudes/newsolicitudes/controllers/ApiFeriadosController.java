package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFeriadosService;

@RestController
@RequestMapping("/api/feriados")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiFeriadosController {

    private final ApiFeriadosService apiFeriadosService;

    public ApiFeriadosController(ApiFeriadosService apiDeriadosService) {
        this.apiFeriadosService = apiDeriadosService;
    }

    @GetMapping
    public ResponseEntity<Object> getFeriados(@RequestParam Integer rut, @RequestParam Integer ident) {
        return ResponseEntity.ok(apiFeriadosService.obtenerFeriadosByRut(rut, ident));

    }

}
