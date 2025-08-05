package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioApiService;

@RestController
@RequestMapping("/api/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class FuncionarioController {

    private final FuncionarioApiService funcionarioApiService;

    public FuncionarioController(FuncionarioApiService funcionarioApiService) {
        this.funcionarioApiService = funcionarioApiService;
    }

    @GetMapping
    public ResponseEntity<Object> obtenerDetalleColaborador(@RequestParam Integer rut, String vRut) {

        return ResponseEntity.ok(funcionarioApiService.getFuncionarioInfo(rut, vRut));
    }

    @GetMapping("/new")
    public ResponseEntity<Object> obtenerDetalleColaboradorNew(@RequestParam Integer rut, String vRut) {

        return ResponseEntity.ok(funcionarioApiService.getFuncionarioInfo(rut, vRut));
    }

}
