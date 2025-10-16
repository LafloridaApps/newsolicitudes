package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.services.apiconsultafirma.ConsultaFirmaService;


@RestController
@RequestMapping("/solicitudes/consulta-firma")
@CrossOrigin(origins = "http://localhost:5173")
public class ConsultaFirmaController {


    private final ConsultaFirmaService consultaFirmaService;

    public ConsultaFirmaController(ConsultaFirmaService consultaFirmaService) {
        this.consultaFirmaService = consultaFirmaService;
    }

    @GetMapping
    public ResponseEntity<Object> consultaFirma(@RequestParam Integer rut){

        return ResponseEntity.ok(consultaFirmaService.consultafirmaUrl(rut));

    }

}
