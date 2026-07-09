package com.newsolicitudes.newsolicitudes.services.decretos;

import com.newsolicitudes.newsolicitudes.config.DocProperties;
import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.exceptions.DocumentException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DocumentoExcelServiceImpl implements DocumentoExcelService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoExcelServiceImpl.class);
    private static final String[] HEADERS = {
        "Nro Decreto", "ID Solicitud", "RUT", "Nombres",
        "Departamento", "Jornada", "Desde", "Hasta",
        "Duracion", "Fecha Solicitud", "Tipo Solicitud", "Tipo Contrato"
    };

    private final DocProperties docProperties;

    public DocumentoExcelServiceImpl(DocProperties docProperties) {
        this.docProperties = docProperties;
    }

    @Override
    public String generarExcel(List<AprobacionList> aprobaciones) {
        logger.info("[generarExcel] Iniciando generacion de Excel para {} aprobaciones", aprobaciones.size());
        if (aprobaciones.isEmpty()) {
            throw new DocumentException("No hay aprobaciones para generar el Excel");
        }

        Long nroDecreto = aprobaciones.get(0).getNroDecreto();
        String rutaDestino = docProperties.getVolumePath() + nroDecreto + "/solicitudes.xlsx";

        crearDirectorio(rutaDestino);
        generarWorkbook(aprobaciones, rutaDestino);

        logger.info("[generarExcel] Excel generado exitosamente en: {}", rutaDestino);
        return rutaDestino;
    }

    private void crearDirectorio(String rutaDestino) {
        Path directorio = Paths.get(rutaDestino).getParent();
        try {
            Files.createDirectories(directorio);
            logger.info("[generarExcel] Directorio creado: {}", directorio);
        } catch (Exception e) {
            throw new DocumentException("No se puede crear el directorio: " + directorio, e);
        }
    }

    private void generarWorkbook(List<AprobacionList> aprobaciones, String rutaDestino) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Solicitudes");
            escribirHeader(sheet, workbook);
            escribirFilas(sheet, aprobaciones);
            autoajustarColumnas(sheet);

            try (OutputStream out = new FileOutputStream(rutaDestino)) {
                workbook.write(out);
            }
        } catch (Exception e) {
            throw new DocumentException("Error al generar Excel: " + e.getMessage(), e);
        }
    }

    private void escribirHeader(Sheet sheet, XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(style);
        }
    }

    private void escribirFilas(Sheet sheet, List<AprobacionList> aprobaciones) {
        int rowNum = 1;
        for (AprobacionList a : aprobaciones) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, a.getNroDecreto());
            setCell(row, 1, a.getIdSolicitud());
            setCell(row, 2, a.getRut());
            setCell(row, 3, a.getNombres());
            setCell(row, 4, a.getDepartamento());
            setCell(row, 5, a.getJornada());
            setCell(row, 6, a.getDesde());
            setCell(row, 7, a.getHasta());
            setCell(row, 8, a.getDuracion());
            setCell(row, 9, a.getFechaSolicitud());
            setCell(row, 10, a.getTipoSolicitud());
            setCell(row, 11, a.getTipoContrato());
        }
    }

    private static void setCell(Row row, int col, Number value) {
        row.createCell(col).setCellValue(value != null ? value.doubleValue() : 0);
    }

    private static void setCell(Row row, int col, String value) {
        row.createCell(col).setCellValue(value != null ? value : "");
    }

    private void autoajustarColumnas(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
