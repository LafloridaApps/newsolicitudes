package com.newsolicitudes.newsolicitudes.services.departamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.apiausencias.ApiAusenciasService;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private static final Logger logger = LoggerFactory.getLogger(DepartamentoServiceImpl.class);

    private final ApiDepartamentoService apiDepartamentoService;

    private final ApiAusenciasService apiAusenciasService;

    private final FuncionarioService funcionarioService;

    private final SolicitudRepository solicitudRepository;

    private final SubroganciaRepository subroganciaRepository;

    public DepartamentoServiceImpl(ApiDepartamentoService apiDepartamentoService,
            ApiAusenciasService apiAusenciasService, FuncionarioService funcionarioService,
            SolicitudRepository solicitudRepository, SubroganciaRepository subroganciaRepository) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.apiAusenciasService = apiAusenciasService;
        this.funcionarioService = funcionarioService;
        this.solicitudRepository = solicitudRepository;
        this.subroganciaRepository = subroganciaRepository;
    }

    @Override
    public DepartamentoResponse getDepartamentoDestino(Integer rutSolicitante, DepartamentoResponse departamentoInicial,
            LocalDate fechaInicio, LocalDate fechaFin) {

        logger.info("Iniciando getDepartamentoDestino para rutSolicitante: {}, departamentoInicial: {}, fechaInicio: {}, fechaFin: {}",
                rutSolicitante, departamentoInicial.getNombre(), fechaInicio, fechaFin);

        DepartamentoResponse dptoActual = departamentoInicial;
        if (rutSolicitante.equals(dptoActual.getRutJefe()) && dptoActual.getIdDeptoSuperior() != null) {
            dptoActual = getSuperior(dptoActual);
        }

        while (dptoActual != null) {

            if (esJefeDisponible(dptoActual, fechaInicio, fechaFin)) {
                logger.info("Jefe de departamento {} disponible. Retornando departamento.", dptoActual.getNombre());
                return dptoActual;
            }

            if (esNivelDireccion(dptoActual)) {
                DepartamentoResponse superior = getSuperior(dptoActual);
                if (superior != null) {
                    return superior;
                } else {
                    return dptoActual;
                }
            }

            DepartamentoResponse superior = getSuperior(dptoActual);
            if (superior == null) {
                return dptoActual;
            }
            dptoActual = superior;
        }

        return null;
    }

    private DepartamentoResponse getSuperior(DepartamentoResponse d) {
        DepartamentoResponse superior = null;
        if (d.getIdDeptoSuperior() != null) {
            superior = apiDepartamentoService.obtenerDepartamento(d.getIdDeptoSuperior());
            logger.debug("Resultado de obtenerDepartamento para superior de {}: {} (ID: {})", d.getNombre(), superior != null ? superior.getNombre() : "null", superior != null ? superior.getId() : "null");
        }
        return superior;
    }

    private boolean esNivelDireccion(DepartamentoResponse d) {
        return d.getNivelDepartamento() != null && "DIRECCION".equalsIgnoreCase(d.getNivelDepartamento());
        
    }

    private boolean esJefeDisponible(DepartamentoResponse d, LocalDate fechaInicio, LocalDate fechaFin) {
        if (d.getRutJefe() == null) {
            return false;
        }

        boolean jefeAusente = isAusenteEnFecha(d.getRutJefe(), fechaInicio);
        boolean jefeConAprobacion = hasAprobacion(d.getRutJefe(), fechaInicio, fechaFin);

        boolean disponible = !jefeAusente && !jefeConAprobacion;

        if (!disponible && esNivelDireccion(d)) { // If chief is not directly available and it's a DIRECCION level department
            boolean hasSubrogate = hasActiveSubrogate(d.getRutJefe(), fechaInicio, fechaFin);
            if (hasSubrogate) {
                return true;
            }
        }

        return disponible;
    }

    private boolean hasActiveSubrogate(Integer rutJefe, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Subrogancia> subrogancias = subroganciaRepository.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, fechaInicio, fechaFin);
        return !subrogancias.isEmpty();
    }

    @Override
    public DepartamentoResponse getDepartamentoById(Long id) {
        return apiDepartamentoService.obtenerDepartamento(id);
    }

    private boolean isAusenteEnFecha(Integer rut, LocalDate fecha) {

        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(rut);
        if (funcionario == null) {
            return true;
        }

        return !apiAusenciasService.getAusenciasByRutAndFechas(funcionario.getRut(),
                funcionario.getIdent(), fecha, fecha).isEmpty();

    }

    private boolean hasAprobacion(Integer rut, LocalDate fechaInicio, LocalDate fechaFin) {

        Optional<List<Solicitud>> optSolicitud = solicitudRepository.findByRutAndFechaInicioBetween(rut, fechaInicio,
                fechaFin);

        boolean hasAprob = false;
        if (optSolicitud.isPresent() && !optSolicitud.get().isEmpty()) {
            hasAprob = optSolicitud.get().stream()
                    .anyMatch(solicitud -> solicitud.getEstado() == Solicitud.EstadoSolicitud.APROBADA &&
                            !solicitud.getFechaInicio().isAfter(fechaFin) &&
                            !solicitud.getFechaTermino().isBefore(fechaInicio));
        }
        logger.debug("Funcionario con rut {} tiene aprobaciones entre {} y {}: {}", rut, fechaInicio, fechaFin, hasAprob);
        return hasAprob;
    }

    @Override
    public List<DepartamentoList> getDepartamentos() {
        return apiDepartamentoService.getDepartamentosList();
    }

}

