package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SearchSubroganciResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.services.searchsubrogancia.SubroganciasSearch;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/solicitudes/subrogancia")
@CrossOrigin(origins = "http://localhost:5173")
public class SubroganciaController {

    private final SubroganciaService subroganciaService;
    private final SubroganciasSearch subroganciasSearch;

    public SubroganciaController(SubroganciaService subroganciaService, SubroganciasSearch subroganciasSearch) {
        this.subroganciaService = subroganciaService;
        this.subroganciasSearch = subroganciasSearch;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createSubrogancia(@RequestBody SubroganciaRequest request) {
        subroganciaService.createSubrogancia(request, request.getFechaInicio(), request.getFechaFin(),
                request.getIdDepto());
        return ResponseEntity.ok(Map.of("message", "Subrogancia creada existosamente"));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> buscarSubrogancia(@RequestParam(required = false) Integer rutJefe,
            @RequestParam(required = false) Integer subrogante,
            @RequestParam(required = false) Long idDepto) {

        List<SearchSubroganciResponse> subrogancias = subroganciasSearch.buscarSubrogancia(rutJefe, subrogante,
                idDepto);
        return ResponseEntity.status(HttpStatus.OK).body(subrogancias);

    }

}
