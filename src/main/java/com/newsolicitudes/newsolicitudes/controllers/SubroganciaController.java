package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;

@RestController
@RequestMapping("/api/subrogancia")
public class SubroganciaController {

    private final SubroganciaService subroganciaService;

    public SubroganciaController(SubroganciaService subroganciaService) {
        this.subroganciaService = subroganciaService;
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crearSubrogancia(@RequestBody SubroganciaRequest request) {
        subroganciaService.createSubrogancia(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Subrogancia creada");
    }

}
