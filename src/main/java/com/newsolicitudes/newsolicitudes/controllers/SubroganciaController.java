package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SearchSubroganciResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.services.searchsubrogancia.SubroganciasSearch;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;

import com.newsolicitudes.newsolicitudes.services.ceropapel.CeroPapelSubroganciaService;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaCreadaResponse;
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
    private final CeroPapelSubroganciaService ceroPapel;

    public SubroganciaController(SubroganciaService subroganciaService, SubroganciasSearch subroganciasSearch,
            CeroPapelSubroganciaService ceroPapel) {
        this.subroganciaService = subroganciaService;
        this.subroganciasSearch = subroganciasSearch;
        this.ceroPapel = ceroPapel;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createSubrogancia(@RequestBody SubroganciaRequest request) {
        Long id = subroganciaService.createSubrogancia(request, request.getFechaInicio(), request.getFechaFin(),
                request.getIdDepto());
        return ResponseEntity.ok(resultado(id));
    }

    private SubroganciaCreadaResponse resultado(Long id) {
        var registro = ceroPapel.consultarRegistro(id);
        return new SubroganciaCreadaResponse(
                "Subrogancia guardada localmente", id,
                registro.getCeroPapelEstado(), registro.getCeroPapelDetalle());
    }

    @GetMapping("/{id}/perfiles-ceropapel")
    public Object consultarPerfiles(@PathVariable Long id) {
        return ceroPapel.consultarOpciones(id);
    }

    public record ConfirmarPerfilRequest(Long perfilId) {
    }

    @PostMapping("/{id}/perfil-ceropapel")
    public Object confirmarPerfil(@PathVariable Long id, @RequestBody ConfirmarPerfilRequest request) {
        ceroPapel.confirmarPerfil(id, request.perfilId());
        return resultado(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> buscarSubrogancia(@RequestParam(required = false) Integer rutJefe,
            @RequestParam(required = false) Integer subrogante,
            @RequestParam(required = false) Long idDepto) {

        List<SearchSubroganciResponse> subrogancias = subroganciasSearch.buscarSubrogancia(rutJefe, subrogante,
                idDepto);
        return ResponseEntity.status(HttpStatus.OK).body(subrogancias);

    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Object> deleteSubroganciaById(@PathVariable Long id) {

        subroganciaService.borrarSubrogancia(id);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Subrogancia eliminada correctamente"));

    }

}
