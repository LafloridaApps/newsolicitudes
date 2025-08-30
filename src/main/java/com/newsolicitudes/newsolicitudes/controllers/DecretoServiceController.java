package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DecretoRequest;
import com.newsolicitudes.newsolicitudes.dto.DecretoDeleteRequest; // New import
import com.newsolicitudes.newsolicitudes.dto.DecretoDto; // New import
import com.newsolicitudes.newsolicitudes.services.decretos.DecretoService;

import java.time.LocalDate; // New import
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping; // New import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam; // New import

@RestController
@RequestMapping("/api/decretos")
@CrossOrigin(origins = "http://localhost:5173")
public class DecretoServiceController {

    private final DecretoService decretoService;

    public DecretoServiceController(DecretoService decretoService) {
        this.decretoService = decretoService;
    }

    @PostMapping("/decretar")
    public ResponseEntity<List<AprobacionList>> decretar(@RequestBody DecretoRequest request) {

        List<AprobacionList> decretadas = decretoService.decretar(request.getIds(), request.getRut());
        return ResponseEntity.status(HttpStatus.CREATED).body(decretadas);
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<Object> revertirDecreto(@RequestBody DecretoDeleteRequest request) {

      
        decretoService.revertirDecreto(request);

        Map<String, Object> response = Map.of("message", "Decreto eliminado correctamente");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/entre-fechas")
    public ResponseEntity<List<DecretoDto>> getDecretosByFecha(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        List<DecretoDto> decretos = decretoService.findDecretosByFecha(fechaInicio, fechaFin);
        return ResponseEntity.ok(decretos);
    }
}
