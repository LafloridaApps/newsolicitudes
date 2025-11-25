package com.newsolicitudes.newsolicitudes.controllers;

import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.derivacion.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.noleidas.NoLeidasService;

@RestController
@RequestMapping("/solicitudes/derivacion")
@CrossOrigin(origins = "http://localhost:5173")
public class DerivacionController {

    private final DerivacionService derivacionService;
    private final NoLeidasService noLeidasService;

    public DerivacionController(DerivacionService derivacionService,
            NoLeidasService noLeidasService) {
        this.derivacionService = derivacionService;
        this.noLeidasService = noLeidasService;
    }

    @PostMapping("/derivar")
    public ResponseEntity<Object> createDerivacion(@RequestParam Long idDerivacion,
            @RequestParam Integer rut) {
        try {

            derivacionService.crearSiguienteDerivacion(idDerivacion, rut);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Derivacion Creada con Exito");
            return ResponseEntity.ok(response);

        } catch (DerivacionExceptions e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/departamento/{idDepto}/page/{pageNumber}")
    public ResponseEntity<Object> getDerivacionesByDeptoId(@RequestParam Integer rut, @PathVariable Long idDepto, @PathVariable int pageNumber, @RequestParam(required = false) Boolean noLeidas) {
        PageSolicitudesResponse solicitudes = derivacionService.getDerivacionesByDeptoId(rut, idDepto, pageNumber, noLeidas);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/no-leidas/{depto}")
    public Long getDerivaciones(@PathVariable Long depto) {

        return noLeidasService.cantidadNoLeidas(depto);

    }

}
