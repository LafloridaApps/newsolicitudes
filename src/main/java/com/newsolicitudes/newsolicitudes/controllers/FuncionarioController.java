package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.utlils.PersonaUtils;

@RestController
@RequestMapping("/api/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping("{rut}")
    public ApiFuncionarioResponse obtenerDetalleColaborador(@PathVariable Integer rut) {

        return funcionarioService.getFuncionarioInfo(rut);
    }

    @GetMapping("/local/{rut}")
    public ResponseEntity<Object> getFuncionarioByRut(@PathVariable Integer rut) {

        boolean rutValido = PersonaUtils.validateRut(rut.toString());

        if (!rutValido) {
            throw new FuncionarioException("Rut inválido");
        }
        rut = Integer.parseInt(rut.toString().substring(0, rut.toString().length() - 1));
        return ResponseEntity.ok(funcionarioService.getFuncionarioByRut(rut));

    }

}
