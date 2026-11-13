package com.newsolicitudes.newsolicitudes.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.dto.ConfiguracionCeroPapelRequest;
import com.newsolicitudes.newsolicitudes.dto.ConfiguracionCeroPapelResponse;
import com.newsolicitudes.newsolicitudes.services.ceropapel.CeroPapelPermisoService;
import com.newsolicitudes.newsolicitudes.services.ceropapel.ConfiguracionCeroPapelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/solicitudes/subrogancia/integracion")
public class CeroPapelConfiguracionController {
    private final ConfiguracionCeroPapelService service;
    private final CeroPapelPermisoService permisos;

    public CeroPapelConfiguracionController(ConfiguracionCeroPapelService service, CeroPapelPermisoService permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    // Solo valores iniciales no secretos; la clave nunca sale del servidor.
    @GetMapping("/valores-iniciales")
    public com.newsolicitudes.newsolicitudes.dto.CeroPapelValoresInicialesResponse valoresIniciales() {
        return service.valoresIniciales();
    }

    @GetMapping("/estado")
    public Map<String, Boolean> estado() {
        return Map.of("habilitada", service.estaHabilitada());
    }

    @GetMapping("/configuracion")
    public ConfiguracionCeroPapelResponse consultar(HttpServletRequest request) {
        permisos.exigirAdministrador(request);
        return service.consultar();
    }

    @PostMapping("/clave")
    public org.springframework.http.ResponseEntity<com.newsolicitudes.newsolicitudes.dto.CeroPapelClaveResponse>
            mostrarClave(HttpServletRequest request) {
        validarCabecera(request);
        permisos.exigirAdministrador(request);
        return org.springframework.http.ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore()).body(service.consultarClave());
    }

    private void validarCabecera(HttpServletRequest request) {
        if (!"1".equals(request.getHeader("X-CeroPapel-Config"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Petición de configuración no válida.");
        }
    }

    @PutMapping("/configuracion")
    public ConfiguracionCeroPapelResponse guardar(@Valid @RequestBody ConfiguracionCeroPapelRequest config,
            HttpServletRequest request) {
        validarCabecera(request);
        permisos.exigirAdministrador(request);
        return service.guardar(config);
    }
}
