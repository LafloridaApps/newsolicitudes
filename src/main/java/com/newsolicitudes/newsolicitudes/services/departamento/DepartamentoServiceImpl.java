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
            logger.info("Solicitante es jefe y departamento inicial tiene superior. Moviendo a superior.");
            dptoActual = getSuperior(dptoActual);
        }

        while (dptoActual != null) {
            logger.info("Evaluando departamento: {} (ID: {}, Nivel: {}, RutJefe: {})",
                    dptoActual.getNombre(), dptoActual.getId(), dptoActual.getNivelDepartamento(), dptoActual.getRutJefe());

            if (esJefeDisponible(dptoActual, fechaInicio, fechaFin)) {
                logger.info("Jefe de departamento {} disponible. Retornando departamento.", dptoActual.getNombre());
                return dptoActual;
            }

            if (esNivelDireccion(dptoActual)) {
                logger.info("Departamento {} es de nivel DIRECCION y su jefe no está disponible. Buscando superior.", dptoActual.getNombre());
                DepartamentoResponse superior = getSuperior(dptoActual);
                if (superior != null) {
                    logger.info("Retornando departamento superior: {}", superior.getNombre());
                    return superior;
                } else {
                    logger.info("Retornando departamento actual (DIRECCION sin superior y jefe no disponible): {}", dptoActual.getNombre());
                    return dptoActual;
                }
            }

            logger.info("Jefe de departamento {} no disponible y no es nivel DIRECCION. Buscando superior.", dptoActual.getNombre());
            DepartamentoResponse superior = getSuperior(dptoActual);
            if (superior == null) {
                logger.info("No hay departamento superior para {}. Retornando departamento actual.", dptoActual.getNombre());
                return dptoActual;
            }
            dptoActual = superior;
        }

        logger.info("Fin de la jerarquía, no se encontró departamento destino. Retornando null.");
        return null;
    }

    private DepartamentoResponse getSuperior(DepartamentoResponse d) {
        logger.debug("Obteniendo superior para departamento: {} (ID: {}, idDeptoSuperior: {})", d.getNombre(), d.getId(), d.getIdDeptoSuperior());
        DepartamentoResponse superior = null;
        if (d.getIdDeptoSuperior() != null) {
            superior = apiDepartamentoService.obtenerDepartamento(d.getIdDeptoSuperior());
            logger.debug("Resultado de obtenerDepartamento para superior de {}: {} (ID: {})", d.getNombre(), superior != null ? superior.getNombre() : "null", superior != null ? superior.getId() : "null");
        }
        return superior;
    }

    private boolean esNivelDireccion(DepartamentoResponse d) {
        boolean isDireccion = d.getNivelDepartamento() != null && "DIRECCION".equalsIgnoreCase(d.getNivelDepartamento());
        logger.debug("Departamento {} es nivel DIRECCION: {}", d.getNombre(), isDireccion);
        return isDireccion;
    }

    private boolean esJefeDisponible(DepartamentoResponse d, LocalDate fechaInicio, LocalDate fechaFin) {
        if (d.getRutJefe() == null) {
            logger.debug("Jefe de departamento {} (Rut: null) no disponible.", d.getNombre());
            return false;
        }

        boolean jefeAusente = isAusenteEnFecha(d.getRutJefe(), fechaInicio);
        boolean jefeConAprobacion = hasAprobacion(d.getRutJefe(), fechaInicio, fechaFin);

        boolean disponible = !jefeAusente && !jefeConAprobacion;

        if (!disponible && esNivelDireccion(d)) { // If chief is not directly available and it's a DIRECCION level department
            boolean hasSubrogate = hasActiveSubrogate(d.getRutJefe(), fechaInicio, fechaFin);
            if (hasSubrogate) {
                logger.debug("Jefe de departamento {} (Rut: {}) no disponible directamente, pero tiene subrogante activo. Considerado disponible.", d.getNombre(), d.getRutJefe());
                return true;
            }
        }

        logger.debug("Jefe de departamento {} (Rut: {}) disponible: {}", d.getNombre(), d.getRutJefe(), disponible);
        return disponible;
    }

    private boolean hasActiveSubrogate(Integer rutJefe, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Subrogancia> subrogancias = subroganciaRepository.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, fechaInicio, fechaFin);
        if (!subrogancias.isEmpty()) {
            logger.debug("Jefe {} tiene subrogante activo.", rutJefe);
            return true;
        }
        logger.debug("Jefe {} no tiene subrogante activo.", rutJefe);
        return false;
    }

    @Override
    public DepartamentoResponse getDepartamentoById(Long id) {
        return apiDepartamentoService.obtenerDepartamento(id);
    }

    private boolean isAusenteEnFecha(Integer rut, LocalDate fecha) {

        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(rut);
        if (funcionario == null) {
            logger.debug("Funcionario con rut {} no encontrado. Considerado ausente.", rut);
            return true;
        }

        boolean ausente = !apiAusenciasService.getAusenciasByRutAndFechas(funcionario.getRut(),
                funcionario.getIdent(), fecha, fecha).isEmpty();
        logger.debug("Funcionario {} (Rut: {}) ausente en fecha {}: {}", funcionario.getNombre(), rut, fecha, ausente);
        return ausente;

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

