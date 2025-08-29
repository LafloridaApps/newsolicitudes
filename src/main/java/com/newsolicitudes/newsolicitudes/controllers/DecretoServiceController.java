package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.DecretoRequest;
import com.newsolicitudes.newsolicitudes.services.decretos.DecretoService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/decretos")
@CrossOrigin(origins = "http://localhost:5173")
public class DecretoServiceController {

    private final DecretoService decretoService;

    public DecretoServiceController(DecretoService decretoService) {
        this.decretoService = decretoService;
    }

    @PostMapping("/decretar")
    public ResponseEntity<Object> decretar(@RequestBody DecretoRequest request) {

        decretoService.decretar(request.getIds(), request.getRut());
        Map<String, String> response = Map.of("message", "Decreto realizado con éxito");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
