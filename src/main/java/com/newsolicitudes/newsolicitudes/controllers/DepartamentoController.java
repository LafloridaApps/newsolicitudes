package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoRequest;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioRequest;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;

@RestController
@RequestMapping("/api/departamentos")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @PostMapping
    public ResponseEntity<Object> createDepartamento(@RequestBody DepartamentoRequest departamentoRequest) {
        try {
            return ResponseEntity.ok(departamentoService.createDepartamento(departamentoRequest));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/list")
    public ResponseEntity<Object> getDepartamentos() {
        try {
            return ResponseEntity.ok(departamentoService.getDepartamentosList());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PutMapping
    public ResponseEntity<Object> updateDepartamento(@RequestParam Long idDepto,
            @RequestBody DepartamentoRequest departamentoRequest) {
        try {
            departamentoService.updateDepartamento(idDepto, departamentoRequest);
            return ResponseEntity.ok("Departamento actualizado correctamente");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PutMapping("/jefe")
    public ResponseEntity<Object> updateJefeDepartamento(@RequestParam Long idDepto,
            @RequestBody FuncionarioRequest request) {
        try {
            departamentoService.updateJefeDeparatmento(idDepto, request.getRut());
            return ResponseEntity.ok("Jefe de departamento actualizado correctamente");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/esjefe")
    public ResponseEntity<Object> isJefeDepartamento(@RequestParam String codEx,
            @RequestParam Integer rut) {
        try {
            return ResponseEntity.ok(departamentoService.isJefeDepartamento(codEx, rut));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}