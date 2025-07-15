package com.newsolicitudes.newsolicitudes.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.EntradaRequest;
import com.newsolicitudes.newsolicitudes.services.interfaces.EntradaService;

@RestController
@RequestMapping("/api/entrada")
@CrossOrigin(origins = "http://localhost:5173")
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

    @PostMapping
    public ResponseEntity<Object> createEntrada(@RequestBody EntradaRequest request){
        
        entradaService.createEntrada(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Entrada Grada correctamante"));
    }

}
