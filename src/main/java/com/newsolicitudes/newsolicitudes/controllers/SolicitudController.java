package com.newsolicitudes.newsolicitudes.controllers;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.SolicitudDetalleDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudService;

@RestController
@RequestMapping("/solicitudes/solicitudes")
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudTercerosService tercerosService;
    private final com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository usuarioRepository;
    private static final String KEY="message";
    private static final Logger logger = LoggerFactory.getLogger(SolicitudController.class);

    public SolicitudController(SolicitudService solicitudService,
            com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository usuarioRepository,
            com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudTercerosService tercerosService) {
        this.tercerosService = tercerosService;
        this.usuarioRepository = usuarioRepository;
        this.solicitudService = solicitudService;
    }

    @PostMapping("/crear")
    public ResponseEntity<Object> crearSolicitud(@RequestBody SolicitudRequest solicitud) {
        if (solicitud != null) solicitud.setRutIngresadoPor(solicitud.getRut());
        logger.info("Recibida petición POST /solicitudes/solicitudes/crear para RUT: {}", solicitud != null ? solicitud.getRut() : "null");
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.createSolicitud(solicitud));
    }

    @PostMapping("/crear-tercero")
    public ResponseEntity<Object> crearSolicitudTercero(@RequestBody SolicitudRequest solicitud,
            @RequestParam String rutGestor) {
        var usuario = usuarioRepository.findByRut(rutGestor).orElse(null);
        boolean autorizado = usuario != null && usuario.getModulos().stream()
                .anyMatch(modulo -> "SOLICITUD POR TERCEROS".equals(modulo.getNombre()));
        if (!autorizado) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(KEY, "No tiene permiso para realizar solicitudes por terceros."));
        }
        tercerosService.validarSolicitante(rutGestor, solicitud.getRut());
        try {
            solicitud.setRutIngresadoPor(Integer.valueOf(rutGestor.replace(".", "").split("-")[0].trim()));
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of(KEY, "El RUT de quien ingresa la solicitud no es válido."));
        }
        logger.info("Solicitud por terceros: gestor {}, solicitante {}", rutGestor, solicitud.getRut());
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.createSolicitud(solicitud));
    }

    @GetMapping("/existe")
    public ResponseEntity<Object> existeSolicitud(@RequestParam Integer rut, LocalDate fechaInicio, String tipo) {
        return ResponseEntity.ok(solicitudService.existeSolicitudByFechaAndTipo(rut, fechaInicio, tipo));
    }

    @GetMapping("/rut")
    public ResponseEntity<Object> getSolicitudesByRut(@RequestParam Integer rut,
            @RequestParam int page,
            @RequestParam int size

    ) {

        return ResponseEntity.ok(solicitudService.getSolicitudesByRut(rut, page, size));

    }

    @GetMapping("/rut-tercero")
    public ResponseEntity<Object> getSolicitudesTercero(@RequestParam String rutGestor,
            @RequestParam Integer rut, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        tercerosService.validarSolicitante(rutGestor, rut);
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(Map.of(KEY, "Página o tamaño de consulta inválidos."));
        }
        return ResponseEntity.ok(solicitudService.getSolicitudesByRut(rut, page, size));
    }

    @GetMapping("by-id")
    public ResponseEntity<Object> getSolicitudById(@RequestParam Long id) {
        SolicitudDetalleDto solicitudDetalleDto = solicitudService.getSolicitudDetalleById(id);
        return ResponseEntity.ok().body(solicitudDetalleDto);
    }

    @PutMapping("/{idSolicitud}")
    public ResponseEntity<Object> updateSolicitud(@PathVariable Long idSolicitud, @RequestBody com.newsolicitudes.newsolicitudes.dto.UpdateSolicitudRequest request) {
        solicitudService.updateSolicitud(idSolicitud, request);
        return ResponseEntity.ok().body("Solicitud actualizada correctamente.");
    }

    @PostMapping("/anular")
    public ResponseEntity<Object> anularSolicitud(@RequestParam Long idSolicitud, @RequestParam String motivo) {
        logger.info("Recibida petición para anular solicitud con id: {} y motivo: {}", idSolicitud, motivo);
        String resultado = solicitudService.anularSolicitud(idSolicitud, motivo);
        Map<String, String> response = new HashMap<>();
        
        if ("REQUIERE_APROBACION".equals(resultado)) {
            response.put("status", "REQUIERE_APROBACION");
            response.put(KEY, "La solicitud ya fue recepcionada. Se ha creado una solicitud de anulación que requiere aprobación de su jefatura.");
            return ResponseEntity.ok().body(response);
        } else {
            response.put("status", "ANULADA_DIRECTAMENTE");
            response.put(KEY, "La solicitud fue anulada exitosamente.");
            return ResponseEntity.ok().body(response);
        }
    }   

    @PostMapping("/aprobar-anulacion")
    public ResponseEntity<Object> resolverSolicitudAnulacion(
            @RequestParam Long idSolicitud,
            @RequestParam Integer rutAprobador) {
        logger.info("Recibida petición para aprobar anulación. ID Solicitud: {}, RUT Aprobador: {}", idSolicitud, rutAprobador);
        String mensaje = solicitudService.resolverSolicitudAnulacion(idSolicitud, rutAprobador);
        return ResponseEntity.ok().body(Map.of(KEY, mensaje));
    }

    @PostMapping("/anular-directo")
    public ResponseEntity<Object> anularSolicitudDirecta(
            @RequestParam Long idSolicitud,
            @RequestParam String motivo,
            @RequestParam Integer rutAprobador) {
        logger.info("Recibida petición para anular directamente solicitud con id: {} por rut: {} y motivo: {}", idSolicitud, rutAprobador, motivo);
        String mensaje = solicitudService.anularSolicitudDirecta(idSolicitud, motivo, rutAprobador);
        return ResponseEntity.ok().body(Map.of(KEY, mensaje));
    }
}
