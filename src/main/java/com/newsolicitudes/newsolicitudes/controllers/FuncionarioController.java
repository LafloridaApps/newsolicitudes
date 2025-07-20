package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SearchFunc;

@RestController
@RequestMapping("/api/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    private final SearchFunc searchFunc;

    public FuncionarioController(FuncionarioService funcionarioService, SearchFunc searchFunc) {
        this.funcionarioService = funcionarioService;
        this.searchFunc = searchFunc;
    }

    @GetMapping
    public ResponseEntity<Object> obtenerDetalleColaborador(@RequestParam Integer rut, String vRut) {

        return ResponseEntity.ok(funcionarioService.getFuncionarioInfo(rut, vRut));
    }

    @GetMapping("/buscar")
    public ResponseEntity<Object> searchFuncByDeptoAndNombre(@RequestParam Long id, String nombre) {

        return ResponseEntity.ok(searchFunc.searchFuncionario(nombre, id));
    }

    @GetMapping("/buscar-director")
    public ResponseEntity<Object> searchDirectorByDepto(@RequestParam Long id) {

        return ResponseEntity.ok(searchFunc.getDirectorActivo(id));
    }

}
