package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.interfaces.ApiAdmistrativoService;

@RestController
@RequestMapping("/api/administrativos")
@CrossOrigin(origins = "http://localhost:5173")
public class AdministrativosController {

    private final ApiAdmistrativoService admistrativoService;

    public AdministrativosController(ApiAdmistrativoService admistrativoService) {
        this.admistrativoService = admistrativoService;
    }

    @GetMapping
    public ResponseEntity<Object> obtenerAdministrativos(@RequestParam Integer rut, @RequestParam Integer ident) {

        return ResponseEntity.ok(admistrativoService.obtenerAdministrativos(rut, ident));
    }

}
