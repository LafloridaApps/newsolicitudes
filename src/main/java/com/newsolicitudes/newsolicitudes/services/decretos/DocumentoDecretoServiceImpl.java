package com.newsolicitudes.newsolicitudes.services.decretos;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.docx4j.XmlUtils;
import jakarta.xml.bind.JAXBElement;

import java.io.ByteArrayOutputStream;

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
    public byte[] generarDocumento(List<AprobacionList> aprobaciones, String templateName) {
        logger.info("Iniciando la generación de documento con template: {}", templateName);

        DocTemplates docTemplate = docTemplatesRepository.findByNombre(templateName);
        if (docTemplate == null) {
            logger.error("No se encontró el template con nombre: {}", templateName);
            throw new DocumentException("No se encontró el template con nombre: " + templateName);
        }
        logger.info("Template encontrado: {}", docTemplate.getDocFile());

        String rutaPlantilla = docProperties.getTemplatesPath().concat(docTemplate.getDocFile());
        logger.info("Ruta de la plantilla: {}", rutaPlantilla);

        try {
            // 1. Cargar plantilla Word (colócala en resources/plantillas/aprobaciones.docx)
            logger.info("Cargando plantilla Word desde: {}", rutaPlantilla);
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(new java.io.FileInputStream(rutaPlantilla));
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
            logger.info("Plantilla cargada correctamente.");

            // 2. Buscar la primera tabla de forma recursiva
            logger.info("Buscando tabla en el documento.");
            List<Object> tables = documentPart.getJAXBNodesViaXPath("//w:tbl", false);
            if (tables.isEmpty()) {
                logger.error("No se encontró ninguna tabla en la plantilla.");
                throw new DocumentException("No se encontró ninguna tabla en la plantilla");
            }
            logger.info("Tabla encontrada.");
            Object obj = tables.get(0);
            Tbl tabla;
            if (obj instanceof JAXBElement) {
                tabla = (Tbl) ((JAXBElement<?>) obj).getValue();
            } else {
                tabla = (Tbl) obj;
            }

            // 3. Tomar la segunda fila como plantilla (fila 0 = encabezado)
            Tr filaEjemplo = (Tr) tabla.getContent().get(1);
            logger.info("Fila de ejemplo obtenida.");

            // 4. Limpiar filas previas (dejamos solo encabezado)
            tabla.getContent().remove(1);
            logger.info("Filas previas limpiadas.");

            // 5. Rellenar filas
            logger.info("Iniciando el relleno de filas.");
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
            logger.info("Relleno de filas completado.");

            // 6. Guardar como Word
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wordMLPackage.save(out);
            logger.info("Documento guardado en ByteArrayOutputStream.");

            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace(); // Imprime el stack trace completo para depuración
            throw new DocumentException("Error generando documento", e);
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
