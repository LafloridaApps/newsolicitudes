package com.newsolicitudes.newsolicitudes.services.decretos;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.springframework.stereotype.Service;
import org.docx4j.XmlUtils;
import org.docx4j.Docx4J;
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

    private final DocProperties docProperties;

    private final DocTemplatesRepository docTemplatesRepository;

    public DocumentoDecretoServiceImpl(DocProperties docProperties, DocTemplatesRepository docTemplatesRepository) {
        this.docProperties = docProperties;
        this.docTemplatesRepository = docTemplatesRepository;
    }

    @Override
    public byte[] generarDocumento(List<AprobacionList> aprobaciones, String templateName) {

        DocTemplates docTemplate = docTemplatesRepository.findByNombre(templateName);

        String  rutaPlantilla = docProperties.getTemplatesPath().concat(docTemplate.getDocFile());

        try {
            // 1. Cargar plantilla Word (colócala en resources/plantillas/aprobaciones.docx)
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(new java.io.FileInputStream(rutaPlantilla));
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

            // 2. Buscar la primera tabla de forma recursiva
            List<Object> tables = documentPart.getJAXBNodesViaXPath("//w:tbl", false);
            if (tables.isEmpty()) {
                throw new DocumentException("No se encontró ninguna tabla en la plantilla");
            }
            Object obj = tables.get(0);
            Tbl tabla;
            if (obj instanceof JAXBElement) {
                tabla = (Tbl) ((JAXBElement<?>) obj).getValue();
            } else {
                tabla = (Tbl) obj;
            }

            // 3. Tomar la segunda fila como plantilla (fila 0 = encabezado)
            Tr filaEjemplo = (Tr) tabla.getContent().get(1);

            // 4. Limpiar filas previas (dejamos solo encabezado)
            tabla.getContent().subList(1, tabla.getContent().size()).clear();

            // 5. Rellenar filas
            for (AprobacionList a : aprobaciones) {
                Tr nuevaFila = XmlUtils.deepCopy(filaEjemplo);

                reemplazarTextoEnCelda(nuevaFila, 0, a.getRut());
                reemplazarTextoEnCelda(nuevaFila, 1, a.getTipoSolicitud());
                reemplazarTextoEnCelda(nuevaFila, 2, a.getDesde());
                reemplazarTextoEnCelda(nuevaFila, 3, a.getHasta());

                tabla.getContent().add(nuevaFila);
            }

            // 6. Convertir a PDF
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Docx4J.toPDF(wordMLPackage, out);

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
