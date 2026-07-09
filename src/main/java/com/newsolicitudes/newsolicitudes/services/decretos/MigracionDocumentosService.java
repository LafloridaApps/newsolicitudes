package com.newsolicitudes.newsolicitudes.services.decretos;

import com.newsolicitudes.newsolicitudes.config.DocProperties;
import com.newsolicitudes.newsolicitudes.entities.Decreto;
import com.newsolicitudes.newsolicitudes.repositories.DecretoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MigracionDocumentosService {

    private static final Logger logger = LoggerFactory.getLogger(MigracionDocumentosService.class);

    private final DecretoRepository decretoRepository;
    private final DocProperties docProperties;

    public MigracionDocumentosService(DecretoRepository decretoRepository, DocProperties docProperties) {
        this.decretoRepository = decretoRepository;
        this.docProperties = docProperties;
    }

    @Transactional(readOnly = true)
    public int migrarDocumentos() {
        java.util.List<Decreto> decretos = decretoRepository.findAll();
        int migrados = 0;

        for (Decreto decreto : decretos) {
            if (debeMigrar(decreto)) {
                try {
                    String ruta = guardarArchivo(decreto.getId(), decreto.getDocumentoPdf());
                    decreto.setDocumentoPath(ruta);
                    decreto.setDocumentoPdf(null);
                    decretoRepository.save(decreto);
                    migrados++;
                    logger.info("Documento del decreto {} migrado a: {}", decreto.getId(), ruta);
                } catch (Exception e) {
                    logger.error("Error al migrar documento del decreto {}: {}", decreto.getId(), e.getMessage());
                }
            }
        }

        logger.info("Migración completada. {} documentos migrados.", migrados);
        return migrados;
    }

    private boolean debeMigrar(Decreto decreto) {
        if (decreto.getDocumentoPdf() == null || decreto.getDocumentoPdf().length == 0) {
            return false;
        }
        if (decreto.getDocumentoPath() != null && !decreto.getDocumentoPath().isBlank()) {
            Path archivoExistente = Paths.get(decreto.getDocumentoPath());
            if (Files.exists(archivoExistente)) {
                return false;
            }
        }
        return true;
    }

    private String guardarArchivo(Long idDecreto, byte[] contenido) throws IOException {
        String rutaBase = docProperties.getVolumePath();
        Path directorio = Paths.get(rutaBase + idDecreto);
        Files.createDirectories(directorio);

        String nombreArchivo = "solicitudes.docx";
        Path archivo = directorio.resolve(nombreArchivo);
        Files.write(archivo, contenido);

        return archivo.toString();
    }
}
