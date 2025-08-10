package com.newsolicitudes.newsolicitudes.controllers;

import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionCreateService;

@RestController
@RequestMapping("/api/derivacion")
@CrossOrigin(origins = "http://localhost:5173")
public class DerivacionController {

    private final DerivacionCreateService derivacionCreateService;
    private final DerivacionService derivacionService;

    public DerivacionController(DerivacionCreateService derivacionCreateService, DerivacionService derivacionService) {
        this.derivacionCreateService = derivacionCreateService;
        this.derivacionService = derivacionService;
    }

    @PostMapping("/derivar/{idDerivacion}")
    public ResponseEntity<Object> createDerivacion(@PathVariable Long idDerivacion) {
        try {

            derivacionCreateService.createDerivacionDepto(idDerivacion);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Derivacion Creada con Exito");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/departamento/{idDepto}/{pageNumber}")
    public ResponseEntity<Object> getDerivacionesByDeptoId(@PathVariable Long idDepto, @PathVariable int pageNumber) {
        PageSolicitudesResponse solicitudes = derivacionService.getDerivacionesByDeptoId(idDepto,pageNumber);
        return ResponseEntity.ok(solicitudes);
    }

}
