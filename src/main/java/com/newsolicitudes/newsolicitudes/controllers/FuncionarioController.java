package com.newsolicitudes.newsolicitudes.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionarioapi.FuncionarioApiService;
import com.newsolicitudes.newsolicitudes.services.resumeninicio.ResmuenFuncInicioService;
import com.newsolicitudes.newsolicitudes.services.searchfunc.SearchFuncServcie;

@RestController
@RequestMapping("/api/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class FuncionarioController {

    private final FuncionarioApiService funcionarioApiService;
    private final ApiDepartamentoService apiDepartamentoService;
    private final SearchFuncServcie searchFuncServcie;
    private final ResmuenFuncInicioService resmuenFuncInicioService;

    public FuncionarioController(FuncionarioApiService funcionarioApiService,
            ApiDepartamentoService apiDepartamentoService,
            SearchFuncServcie searchFuncServcie,
            ResmuenFuncInicioService resmuenFuncInicioService) {
        this.funcionarioApiService = funcionarioApiService;
        this.apiDepartamentoService = apiDepartamentoService;
        this.searchFuncServcie = searchFuncServcie;
        this.resmuenFuncInicioService = resmuenFuncInicioService;
    }

    @GetMapping
    public ResponseEntity<Object> obtenerDetalleColaborador(@RequestParam Integer rut) {
        return ResponseEntity.ok(funcionarioApiService.getFuncionarioInfo(rut));
    }

    @GetMapping("/cargofunc")
    public ResponseEntity<Object> getCargFunc(@RequestParam Long codDepto, Integer rut) {
        return ResponseEntity.ok(apiDepartamentoService.obtenerJefeFunc(codDepto, rut));
    }

    @GetMapping("/director-activo")
    public ResponseEntity<Object> getDirectorActivo(
            @RequestParam Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(searchFuncServcie.getDirectorActivo(id, fechaInicio, fechaFin));
    }

    @GetMapping("/subrogante-rut")
    public ResponseEntity<Object> getSubroganteRut(
            @RequestParam Integer rut,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        return ResponseEntity.ok(searchFuncServcie.buscarSubroganteByRut(rut, fechaInicio, fechaFin));
    }

    @GetMapping("/subrogante-nombre")
    public ResponseEntity<Object> getFuncionarioByNombre(
            @RequestParam String nombre,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam int pageNumber,
            @RequestParam Long idDepto) {
        return ResponseEntity
                .ok(searchFuncServcie.buscarFuncionarioByNombre(nombre, fechaInicio, fechaFin, pageNumber, idDepto));
    }

    @GetMapping("/resumen-inicio")
    public ResponseEntity<Object> getResumenFuncInicio(
            @RequestParam Integer rut) {

        return ResponseEntity.ok(resmuenFuncInicioService.getResumen(rut));
    }
}
