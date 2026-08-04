package com.newsolicitudes.newsolicitudes.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DecretoConSolicitudesDTO;
import com.newsolicitudes.newsolicitudes.dto.DecretoRequest;
import com.newsolicitudes.newsolicitudes.dto.DecretoDeleteRequest;
import com.newsolicitudes.newsolicitudes.dto.DecretoDto;
import com.newsolicitudes.newsolicitudes.services.decretos.DecretoService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/solicitudes/decretos")
@CrossOrigin(origins = "http://localhost:5173")
public class DecretoServiceController {

    private static final Logger log = LoggerFactory.getLogger(DecretoServiceController.class);
    private static final String MESSAGE = "message";


    private final DecretoService decretoService;
    private final com.newsolicitudes.newsolicitudes.services.decretos.MigracionDocumentosService migracionService;

    public DecretoServiceController(DecretoService decretoService,
            com.newsolicitudes.newsolicitudes.services.decretos.MigracionDocumentosService migracionService) {
        this.decretoService = decretoService;
        this.migracionService = migracionService;
    }

    @PostMapping("/decretar")
    public ResponseEntity<Map<String, Object>> decretar(@RequestBody DecretoRequest request) {
        log.info("===== INICIO decretar =====");
        log.info("IDs: {}, rut: {}, template: {}", request.getIds(), request.getRut(), request.getTemplate());

        try {
            List<AprobacionList> decretadas = decretoService.decretar(request.getIds(), request.getRut(),
                    request.getTemplate());
            log.info("Se decretaron {} solicitudes", decretadas.size());

            String rutaWord = null;
            String rutaExcel = null;
            if (!decretadas.isEmpty()) {
                Long idDecreto = decretadas.get(0).getNroDecreto();
                log.info("ID del decreto generado: {}", idDecreto);
                rutaWord = decretoService.getDecretoDocumentoPath(idDecreto);
                log.info("rutaWord: {}", rutaWord);
                if (rutaWord != null) {
                    rutaExcel = rutaWord.replace("solicitudes.docx", "solicitudes.xlsx");
                    log.info("rutaExcel: {}", rutaExcel);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("solicitudes", decretadas);
            response.put("rutaWord", rutaWord);
            response.put("rutaExcel", rutaExcel);

            log.info("===== FIN decretar OK =====");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("===== ERROR en decretar =====", e);
            log.error("Request: ids={}, rut={}, template={}", request.getIds(), request.getRut(), request.getTemplate());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put(MESSAGE, e.getMessage());
            errorResponse.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<Object> revertirDecreto(@RequestBody DecretoDeleteRequest request) {

        decretoService.revertirDecreto(request);

        Map<String, Object> response = Map.of(MESSAGE, "Decreto eliminado correctamente");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> getDecretoExcel(@RequestParam Long id) {
        byte[] documento = decretoService.getDecretoExcelDocumento(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "solicitudes-" + id + ".xlsx");
        headers.setContentLength(documento.length);

        return new ResponseEntity<>(documento, headers, HttpStatus.OK);
    }

    @GetMapping("/entre-fechas")
    public ResponseEntity<List<DecretoDto>> getDecretosByFecha(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        List<DecretoDto> decretos = decretoService.findDecretosByFecha(fechaInicio, fechaFin);
        return ResponseEntity.ok(decretos);
    }

    @GetMapping("/documento")
    public ResponseEntity<byte[]> getDecretoDocumento(@RequestParam Long id) {
        byte[] documento = decretoService.getDecretoDocumento(id);

        if (documento == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", "decreto-" + id + ".docx");
        headers.setContentLength(documento.length);

        return new ResponseEntity<>(documento, headers, HttpStatus.OK);
    }

    @GetMapping("/documento/ruta")
    public ResponseEntity<Map<String, String>> getDecretoDocumentoPath(@RequestParam Long id) {
        String path = decretoService.getDecretoDocumentoPath(id);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("ruta", path));
    }

    @PostMapping("/migrar")
    public ResponseEntity<Map<String, Object>> migrarDocumentos() {
        int migrados = migracionService.migrarDocumentos();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Migración completada",
                "documentosMigrados", migrados));
    }

    @PostMapping("/reporte/{idDecreto}")
    public ResponseEntity<Map<String, String>> generarReporte(
            @PathVariable Long idDecreto,
            @RequestParam(defaultValue = "aprobaciones") String template) {
        String ruta = decretoService.generarReporteSolicitudes(idDecreto, template);
        return ResponseEntity.ok(Map.of("ruta", ruta, "mensaje", "Reporte generado correctamente"));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DecretoConSolicitudesDTO>> searchDecretos(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer rut,
            @RequestParam(required = false) Long idSolicitud,
            @RequestParam(required = false) String nombreFuncionario,
            Pageable pageable) {

        Page<DecretoConSolicitudesDTO> resultados = decretoService.searchDecretos(id, fechaDesde, fechaHasta, rut, idSolicitud, nombreFuncionario, pageable);
        return ResponseEntity.ok(resultados);
    }
}
