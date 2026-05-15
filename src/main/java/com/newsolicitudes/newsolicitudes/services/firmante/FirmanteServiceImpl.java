package com.newsolicitudes.newsolicitudes.services.firmante;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;

@Service
public class FirmanteServiceImpl implements FirmanteService {

    private final ApiDepartamentoService apiDepartamentoService;
    private final SubroganciaRepository subroganciaRepository;
    private final AprobacionRepository aprobacionRepository;
    private static final Logger logger = LoggerFactory.getLogger(FirmanteServiceImpl.class);


    public FirmanteServiceImpl(ApiDepartamentoService apiDepartamentoService, SubroganciaRepository subroganciaRepository, AprobacionRepository aprobacionRepository) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.subroganciaRepository = subroganciaRepository;
        this.aprobacionRepository = aprobacionRepository;
    }

    @Override
    public Integer getRutFirmante(Solicitud solicitud) {
        Integer rutAprobacion = getRutFromAprobacion(solicitud);
        if (rutAprobacion != null) {
            return rutAprobacion;
        }

        return calculateRutFirmante(solicitud);
    }

    private Integer getRutFromAprobacion(Solicitud solicitud) {
        if (solicitud.getEstado() == EstadoSolicitud.APROBADA) {
            logger.info("Solicitud {} ya está APROBADA. Buscando firmante en la tabla Aprobacion.", solicitud.getId());
            Optional<Aprobacion> aprobacionOpt = aprobacionRepository.findBySolicitud(solicitud);
            if (aprobacionOpt.isPresent()) {
                Integer rutFirmanteAprobacion = aprobacionOpt.get().getRut();
                logger.info("Firmante encontrado en Aprobacion: {}. Retornando este RUT.", rutFirmanteAprobacion);
                return rutFirmanteAprobacion;
            }
            logger.warn("Solicitud {} está APROBADA pero no se encontró registro en Aprobacion. Se procederá con el cálculo normal.", solicitud.getId());
        }
        return null;
    }

    private Integer calculateRutFirmante(Solicitud solicitud) {
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());
        while (departamento != null) {
            logger.info("Procesando departamento: idDepto={}, nombre={}, rutJefe={}, nivelDepartamento={}",
                departamento.getId(), departamento.getNombre(), departamento.getRutJefe(), departamento.getNivelDepartamento());

            if (isSigningDepartment(departamento)) {
                logger.info("Departamento {} es firmante. Solicitud: fechaInicio={}, fechaTermino={}",
                    departamento.getNombre(), solicitud.getFechaInicio(), solicitud.getFechaTermino());
                
                if (isJefeSolicitante(departamento, solicitud)) {
                    if (departamento.getIdDeptoSuperior() == null) {
                        logger.info("Jefe {} es el solicitante y no hay departamento superior. Firma él mismo.", departamento.getRutJefe());
                        return departamento.getRutJefe();
                    }
                    logger.info("Jefe {} es el solicitante. Subiendo al departamento superior.", departamento.getRutJefe());
                } else {
                    Integer firmanteRut = findSignerInDepartment(departamento, solicitud);
                    logger.info("Firmante encontrado en departamento {}: {}", departamento.getNombre(), firmanteRut);
                    return firmanteRut;
                }
            } else {
                logger.info("Departamento {} no es firmante. Se evaluará el departamento superior.", departamento.getNombre());
            }
            
            // Lógica unificada para subir al departamento superior sin usar break o continue
            if (departamento.getIdDeptoSuperior() != null) {
                departamento = apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
            } else {
                logger.info("Departamento {} no tiene departamento superior. Terminando búsqueda.", departamento.getNombre());
                departamento = null;
            }
        }
        return null;
    }

    private boolean isJefeSolicitante(DepartamentoResponse departamento, Solicitud solicitud) {
        return departamento.getRutJefe() != null && departamento.getRutJefe().equals(solicitud.getRut());
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
            
            if (rutSubrogante.equals(solicitud.getRut())) {
                logger.info("El subrogante ({}) es el propio solicitante. Retornando el RUT del jefe titular ({}).", rutSubrogante, departamento.getRutJefe());
                return departamento.getRutJefe();
            }
            
            logger.info("Subrogancia encontrada. Jefe: {}, Subrogante: {}", departamento.getRutJefe(), rutSubrogante);
            return rutSubrogante;
        } else {
            logger.info("No se encontró subrogancia para jefe {}. Se retorna el RUT del jefe.", departamento.getRutJefe());
            return departamento.getRutJefe();
        }
    }
}