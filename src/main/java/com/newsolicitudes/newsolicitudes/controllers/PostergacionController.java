package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.PostergacionRequest;
import com.newsolicitudes.newsolicitudes.services.interfaces.PostergacionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@RestController
@RequestMapping("/api/postergaciones")
@CrossOrigin(origins = "http://localhost:5173")
public class PostergacionController {

    private final PostergacionService postergacionService;

    public PostergacionController(PostergacionService postergacionService) {
        this.postergacionService = postergacionService;
    }

    @PostMapping("/crear")
    public ResponseEntity<Object> createPostergacion(@RequestBody PostergacionRequest request) {

        postergacionService.createPostergacion(request.getIdSolicitud(), request.getMotivo(),
                request.getPostergadoPor());

        Map<String, String> response = Map.of("message", "Solicitud postergada con éxito");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
