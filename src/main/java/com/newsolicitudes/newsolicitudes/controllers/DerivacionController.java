package com.newsolicitudes.newsolicitudes.controllers;

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
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;

@RestController
@RequestMapping("/api/derivacion")
@CrossOrigin(origins = "http://localhost:5173")
public class DerivacionController {

    private final DerivacionService derivacionService;

    private final DerivacionCreateService derivacionCreateService;

    public DerivacionController(DerivacionService derivacionService, DerivacionCreateService derivacionCreateService) {
        this.derivacionService = derivacionService;
        this.derivacionCreateService = derivacionCreateService;
    }

    @GetMapping("/{rut}")
    public ResponseEntity<Object> getDerivacionesByRut(@PathVariable Integer rut) {
        try {
            return ResponseEntity.ok(derivacionService.getDerivacionesByFuncionario(rut));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

}
