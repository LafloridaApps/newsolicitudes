package com.newsolicitudes.newsolicitudes.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoDto;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.RecordDepartamentoRequest;
import com.newsolicitudes.newsolicitudes.services.apiextdepartamentos.DepartamentoExternoService;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;

@RestController
@RequestMapping("/solicitudes/departamentos")
@CrossOrigin(origins = {"http://localhost:5173", "https://appx.laflorida.cl", "https://intranet.laflorida.cl"},
        allowCredentials = "true")
public class DepartamentoController {

    private final com.newsolicitudes.newsolicitudes.services.departamento.PermisoAsociacionDepartamentoService permisos;
    private final DepartamentoService departamentoService;
    private final DepartamentoExternoService departamentoExternoService;
    private static final String MESSAGE = "message";

    public DepartamentoController(DepartamentoService departamentoService,
            DepartamentoExternoService departamentoExternoService,
            com.newsolicitudes.newsolicitudes.services.departamento.PermisoAsociacionDepartamentoService permisos) {
        this.permisos = permisos;
        this.departamentoService = departamentoService;
        this.departamentoExternoService = departamentoExternoService;
    }

    @GetMapping("/permiso-asociacion")
    public Map<String, Boolean> permisoAsociacion(jakarta.servlet.http.HttpServletRequest request) {
        return Map.of("permitido", permisos.puedeAdministrar(request));
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getDepartamentos() {

        List<DepartamentoList> response = departamentoService.getDepartamentos();

        if (response.isEmpty()) {
            return ResponseEntity.ok().body("No se encontraron departamanetos");
        }

        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/{idDepto}")
    public ResponseEntity<Object> updteJefeDepartamento(@PathVariable Long idDepto,
            @RequestParam Integer rut) {

        departamentoService.updateJefeDepartamento(idDepto, rut);
        return ResponseEntity.ok(Map.of(MESSAGE, "Rut actualizado con exito"));
    }

    @PutMapping("/{idDepto}/nombre")
    public ResponseEntity<Object> updteJefeDepartamento(@PathVariable Long idDepto,
            @RequestParam String nombre) {

        if (nombre == null || nombre.isBlank() || nombre.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE, "El nombre edl departamento no puede ser nulo"));
        }
        departamentoService.updateNombreDepartamento(idDepto, nombre);
        return ResponseEntity.ok(Map.of(MESSAGE, "Nombre departamento actualizado con exito"));
    }

    @PutMapping("/{idDepto}/codex")
    public ResponseEntity<Object> updateCodigoExternoDepartamento(@PathVariable Long idDepto,
            @RequestParam String codEx, jakarta.servlet.http.HttpServletRequest request) {
        permisos.exigirAdministrador(request);
        if (idDepto == null || idDepto <= 0 || codEx == null || !codEx.matches("[0-9]{1,10}")) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE, "Indique un departamento y un código externo numérico de hasta 10 dígitos."));
        }

        try {
            departamentoService.updateCodigoExterno(idDepto, codEx);
            return ResponseEntity.ok(Map.of(MESSAGE, "Codigo Externo Actualizado con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE, e.getMessage()));
        }

    }

    @GetMapping("/list-externo")
    public ResponseEntity<Object> getDepartamentosExternosList() {

        List<DepartamentoDto> response = departamentoExternoService.getDepartamentosExternosList();

        if (response.isEmpty()) {
            return ResponseEntity.ok().body("No se encontraron departamanetos");
        }

        return ResponseEntity.ok().body(response);

    }

    @DeleteMapping("/{idDepto}/codex")
    public ResponseEntity<Object> deleteCodigoExtgernoByIdDepto(@PathVariable Long idDepto,
            jakarta.servlet.http.HttpServletRequest request) {
        permisos.exigirAdministrador(request);
        departamentoService.delteCodigoExternoByIdDepto(idDepto);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(MESSAGE, "Código eliminado con éxito"));
    }

    @PostMapping("/add")
    public ResponseEntity<Object> agregarDepartamento(@RequestBody RecordDepartamentoRequest request) {

        departamentoService.agregarDepartamento(request);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(MESSAGE, "Departamento agregado con éxito"));

    }
}
