package com.newsolicitudes.newsolicitudes.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;

@RestController
@RequestMapping("/solicitudes/departamentos")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getDepartamentos() {

        List<DepartamentoList> response = departamentoService.getDepartamentos();

        if (response.isEmpty()) {
            return ResponseEntity.ok().body("No se encontraron departamanetos");
        }

        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/update/{idDepto}")
    public ResponseEntity<Object> updteJefeDepartamento(@PathVariable Long idDepto,
            @RequestBody Map<String, Integer> requet) {

        Integer rut = requet.get("rut");

        departamentoService.updateJefeDepartamento(idDepto, rut);
        return ResponseEntity.ok(Map.of("message", "Rut actualizado con exito"));
    }

}
