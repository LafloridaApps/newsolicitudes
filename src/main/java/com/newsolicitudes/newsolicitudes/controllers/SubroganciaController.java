package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;

import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/subrogancia")
@CrossOrigin(origins = "http://localhost:5173")
public class SubroganciaController {

    private final SubroganciaService subroganciaService;

    public SubroganciaController(SubroganciaService subroganciaService) {
        this.subroganciaService = subroganciaService;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createSubrogancia(@RequestBody SubroganciaRequest request) {
        subroganciaService.createSubrogancia(request, request.getFechaInicio(), request.getFechaFin(),
                request.getIdDepto());
        return ResponseEntity.ok(Map.of("message", "Subrogancia creada existosamente"));
    }

}
