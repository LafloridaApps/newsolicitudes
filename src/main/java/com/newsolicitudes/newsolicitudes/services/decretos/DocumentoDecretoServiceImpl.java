package com.newsolicitudes.newsolicitudes.services.decretos;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.docx4j.XmlUtils;
import jakarta.xml.bind.JAXBElement;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import com.newsolicitudes.newsolicitudes.config.DocProperties;
import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.entities.DocTemplates;
import com.newsolicitudes.newsolicitudes.exceptions.DocumentException;
import com.newsolicitudes.newsolicitudes.repositories.DocTemplatesRepository;

@Service
public class DocumentoDecretoServiceImpl implements DocumentoDecretoService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoDecretoServiceImpl.class);

    private final DocProperties docProperties;

    private final DocTemplatesRepository docTemplatesRepository;

    public DocumentoDecretoServiceImpl(DocProperties docProperties, DocTemplatesRepository docTemplatesRepository) {
        this.docProperties = docProperties;
        this.docTemplatesRepository = docTemplatesRepository;
    }

    @Override
    public String generarDocumento(List<AprobacionList> aprobaciones, String templateName) {
        logger.info("[generarDocumento] Iniciando generación de documento con template: {}", templateName);

        if (aprobaciones.isEmpty()) {
            logger.error("[generarDocumento] No hay aprobaciones para generar el documento");
            throw new DocumentException("No hay aprobaciones para generar el documento");
        }

        DocTemplates docTemplate = docTemplatesRepository.findByNombre(templateName);
        if (docTemplate == null) {
            logger.error("[generarDocumento] No se encontró el template con nombre: {}", templateName);
            throw new DocumentException("No se encontró el template con nombre: " + templateName);
        }
        logger.info("[generarDocumento] Template encontrado: {}", docTemplate.getDocFile());

        Long nroDecreto = aprobaciones.get(0).getNroDecreto();
        String rutaPlantilla = docProperties.getTemplatesPath().concat(docTemplate.getDocFile());
        String rutaVolumen = docProperties.getVolumePath();
        String nombreArchivo = "solicitudes.docx";
        String rutaDestino = rutaVolumen + nroDecreto + "/" + nombreArchivo;
        logger.info("[generarDocumento] Ruta plantilla: {}", rutaPlantilla);
        logger.info("[generarDocumento] Ruta volumen: {}", rutaVolumen);
        logger.info("[generarDocumento] Ruta destino: {}", rutaDestino);

        // Verificar que el template existe
        java.io.File plantillaFile = new java.io.File(rutaPlantilla);
        if (!plantillaFile.exists()) {
            logger.error("[generarDocumento] El archivo de plantilla NO existe: {}", rutaPlantilla);
            throw new DocumentException("No se encuentra la plantilla: " + rutaPlantilla);
        }
        logger.info("[generarDocumento] Plantilla existe: {} ({} bytes)", rutaPlantilla, plantillaFile.length());

        // Verificar que el directorio de volumen existe o crearlo
        Path directorio = Paths.get(rutaVolumen);
        if (!Files.exists(directorio)) {
            logger.warn("[generarDocumento] El directorio de volumen NO existe, creando: {}", rutaVolumen);
            try {
                Files.createDirectories(directorio);
            } catch (Exception e) {
                logger.error("[generarDocumento] No se pudo crear el directorio de volumen: {}", rutaVolumen, e);
                throw new DocumentException("No se puede crear el directorio de destino: " + rutaVolumen);
            }
        }

        try {
            Path directorioDecreto = Paths.get(rutaVolumen + nroDecreto);
            Files.createDirectories(directorioDecreto);
            logger.info("[generarDocumento] Directorio del decreto creado: {}", directorioDecreto);

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(new java.io.FileInputStream(rutaPlantilla));
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
            logger.info("[generarDocumento] Plantilla cargada correctamente.");

            List<Object> tables = documentPart.getJAXBNodesViaXPath("//w:tbl", false);
            if (tables.isEmpty()) {
                logger.error("[generarDocumento] No se encontró ninguna tabla en la plantilla.");
                throw new DocumentException("No se encontró ninguna tabla en la plantilla");
            }
            logger.info("[generarDocumento] {} tabla(s) encontrada(s)", tables.size());
            Object obj = tables.get(0);
            Tbl tabla;
            if (obj instanceof JAXBElement) {
                tabla = (Tbl) ((JAXBElement<?>) obj).getValue();
            } else {
                tabla = (Tbl) obj;
            }

            Tr filaEjemplo = (Tr) tabla.getContent().get(1);
            logger.info("[generarDocumento] Fila de ejemplo obtenida.");

            tabla.getContent().remove(1);
            logger.info("[generarDocumento] Filas previas limpiadas.");

            logger.info("[generarDocumento] Rellenando {} filas...", aprobaciones.size());
            int contador = 1;
            for (AprobacionList a : aprobaciones) {
                Tr nuevaFila = XmlUtils.deepCopy(filaEjemplo);

                reemplazarTextoEnCelda(nuevaFila, 0, String.valueOf(contador));
                reemplazarTextoEnCelda(nuevaFila, 1, a.getNombres());
                reemplazarTextoEnCelda(nuevaFila, 2, a.getRut());
                reemplazarTextoEnCelda(nuevaFila, 3, a.getDesde());
                reemplazarTextoEnCelda(nuevaFila, 4, a.getHasta());
                reemplazarTextoEnCelda(nuevaFila, 5, a.getJornada());
                reemplazarTextoEnCelda(nuevaFila, 6, a.getDuracion().toString());
                reemplazarTextoEnCelda(nuevaFila, 7, a.getNroDecreto().toString());
                reemplazarTextoEnCelda(nuevaFila, 8, a.getIdSolicitud().toString());

                tabla.getContent().add(nuevaFila);
                contador++;
            }
            logger.info("[generarDocumento] Relleno de filas completado.");

            try (OutputStream out = new FileOutputStream(rutaDestino)) {
                wordMLPackage.save(out);
            }
            logger.info("[generarDocumento] Documento guardado exitosamente en: {}", rutaDestino);

            return rutaDestino;

        } catch (DocumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[generarDocumento] Error generando documento", e);
            throw new DocumentException("Error generando documento: " + e.getMessage(), e);
        }

    }

    private void reemplazarTextoEnCelda(Tr fila, int indexCelda, String texto) {
        Object obj = fila.getContent().get(indexCelda);
        Tc celda;
        if (obj instanceof JAXBElement) {
            celda = (Tc) ((JAXBElement<?>) obj).getValue();
        } else {
            celda = (Tc) obj;
        }
        celda.getContent().clear();

        P parrafo = new P();
        R run = new R();
        Text t = new Text();
        t.setValue(texto);

        run.getContent().add(t);
        parrafo.getContent().add(run);
        celda.getContent().add(parrafo);
    }
}
