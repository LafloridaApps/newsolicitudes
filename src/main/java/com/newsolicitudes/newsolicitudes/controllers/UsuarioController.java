package com.newsolicitudes.newsolicitudes.controllers;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioModuloRequest;
import com.newsolicitudes.newsolicitudes.services.usuario.UsuarioService;

@RestController
@RequestMapping("/solicitudes/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/pagina")
    public ResponseEntity<Page<UsuarioDto>> listarPagina(
            @RequestParam(defaultValue = "") String busqueda,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarPagina(busqueda.trim(), pageable));
    }

    @PostMapping("/permisos")
    public ResponseEntity<Void> guardarPermisos(@RequestBody UsuarioModuloRequest request) {
        usuarioService.guardarUsuarioConModulos(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{rut}/modulos")
    public ResponseEntity<List<ModuloDto>> obtenerModulos(@PathVariable String rut) {
        List<ModuloDto> modulos = usuarioService.obtenerModulosPorUsuario(rut);
        return ResponseEntity.ok(modulos);
    }

    @GetMapping("/{rut}")
    public ResponseEntity<UsuarioDto> obtenerUsuario(@PathVariable String rut) {
        UsuarioDto usuario = usuarioService.obtenerUsuarioPorRut(rut);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuarios() {
        List<UsuarioDto> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/crear-o-actualizar")
    public ResponseEntity<Object> crearOActualizarUsuario(@RequestBody UsuarioModuloRequest usuarioDto) {
        usuarioService.createOrUpdateUsuario(usuarioDto.getRut(), usuarioDto.getModulos());
        return ResponseEntity.ok().body(Map.of("message", "Usuario creado o actualizado con éxito"));
    }
}