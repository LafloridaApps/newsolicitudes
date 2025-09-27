package com.newsolicitudes.newsolicitudes.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.services.modulo.ModuloService;

@RestController
@RequestMapping("/solicitudes/modulos")
@CrossOrigin(origins = "http://localhost:5173")
public class ModuloController {

    private final ModuloService moduloService;

    public ModuloController(ModuloService moduloService) {
        this.moduloService = moduloService;
    }

    @PostMapping("/crear")
    public ResponseEntity<Modulo> crearModulo(@RequestBody ModuloDto moduloDto) {
        Modulo nuevoModulo = moduloService.crearModulo(moduloDto);
        return new ResponseEntity<>(nuevoModulo, HttpStatus.CREATED);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ModuloDto>> listarModulos() {
        List<ModuloDto> modulos = moduloService.listarModulos();
        return ResponseEntity.ok(modulos);
    }
}
