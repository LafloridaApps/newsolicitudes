package com.newsolicitudes.newsolicitudes.services.firmante;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;

@Service
public class FirmanteServiceImpl implements FirmanteService {

    private final ApiDepartamentoService apiDepartamentoService;
    private final SubroganciaRepository subroganciaRepository;
    private static final Logger logger = LoggerFactory.getLogger(FirmanteServiceImpl.class);


    public FirmanteServiceImpl(ApiDepartamentoService apiDepartamentoService, SubroganciaRepository subroganciaRepository) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.subroganciaRepository = subroganciaRepository;
    }

    @Override
    public Integer getRutFirmante(Solicitud solicitud) {
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());
        while (departamento != null) {
            logger.info("Procesando departamento: idDepto={}, nombre={}, rutJefe={}, nivelDepartamento={}",
                departamento.getId(), departamento.getNombre(), departamento.getRutJefe(), departamento.getNivelDepartamento());
            if (isSigningDepartment(departamento)) {
                logger.info("Departamento {} es firmante. Solicitud: fechaInicio={}, fechaTermino={}",
                    departamento.getNombre(), solicitud.getFechaInicio(), solicitud.getFechaTermino());
                Integer firmanteRut = findSignerInDepartment(departamento, solicitud);
                if (firmanteRut.equals(departamento.getRutJefe()) && departamento.getRutJefe().equals(solicitud.getRut()) && departamento.getIdDeptoSuperior() != null) {
                    // Si el jefe es el solicitante y no hay subrogancia, y hay un departamento superior, subimos.
                    logger.info("Jefe {} es el solicitante y no hay subrogancia. Subiendo al departamento superior.", departamento.getRutJefe());
                    departamento = apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
                } else {
                    // Si hay un subrogante, o el jefe no es el solicitante, o el jefe es el solicitante pero no hay departamento superior, este es el firmante.
                    logger.info("Firmante encontrado en departamento {}: {}", departamento.getNombre(), firmanteRut);
                    return firmanteRut;
                }
            } else if (departamento.getIdDeptoSuperior() != null) {
                logger.info("Departamento {} no es firmante. Subiendo al departamento superior.", departamento.getNombre());
                departamento = apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
            } else {
                logger.info("Departamento {} no es firmante y no tiene departamento superior. Terminando búsqueda.", departamento.getNombre());
                departamento = null;
            }
        }
        return null;
    }

    private boolean isSigningDepartment(DepartamentoResponse departamento) {
        if (departamento == null || departamento.getNivelDepartamento() == null) {
            return false;
        }
        String nivel = departamento.getNivelDepartamento();
        return nivel.equalsIgnoreCase("DIRECCION") || nivel.equalsIgnoreCase("ALCALDIA")
                || nivel.equalsIgnoreCase("ADMINISTRACION") || nivel.equalsIgnoreCase("SUBDIRECCION");
    }

    private Integer findSignerInDepartment(DepartamentoResponse departamento, Solicitud solicitud) {
        logger.info("Buscando firmante para depto: {}, jefe: {}, solicitud desde: {}, hasta: {}",
            departamento.getNombre(), departamento.getRutJefe(), solicitud.getFechaInicio(), solicitud.getFechaTermino());

        Optional<Subrogancia> subrogancia = subroganciaRepository
                .findFirstByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        departamento.getRutJefe(), LocalDate.now(), LocalDate.now());

        if (subrogancia.isPresent()) {
            Integer rutSubrogante = subrogancia.get().getSubrogante();
            logger.info("Subrogancia encontrada. Jefe: {}, Subrogante: {}", departamento.getRutJefe(), rutSubrogante);
            return rutSubrogante;
        } else {
            logger.info("No se encontró subrogancia para jefe {}. Se retorna el RUT del jefe.", departamento.getRutJefe());
            return departamento.getRutJefe();
        }
    }
}