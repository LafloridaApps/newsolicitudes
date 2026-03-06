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

        DepartamentoResponse dptoActual = departamentoInicial;

        // Verificar si el solicitante está subrogando actualmente a otro departamento.
        // Si es así, el flujo debe comenzar desde el superior del departamento subrogado.
        List<Subrogancia> subroganciasActivas = subroganciaRepository
                .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutSolicitante, LocalDate.now(), LocalDate.now());

        if (!subroganciasActivas.isEmpty()) {
            Long idDeptoSubrogado = subroganciasActivas.get(0).getIdDepto();
            DepartamentoResponse deptoSubrogado = getDepartamentoById(idDeptoSubrogado);
            if (deptoSubrogado != null) {
                dptoActual = getSuperior(deptoSubrogado);
            }
        } else {
            // Si el solicitante es su propio jefe (titular), empezar por el superior.
            if (rutSolicitante.equals(dptoActual.getRutJefe()) && dptoActual.getIdDeptoSuperior() != null) {
                dptoActual = getSuperior(dptoActual);
            }
        }

        while (dptoActual != null) {
            DepartamentoResponse resultado = procesarDepartamento(dptoActual, rutSolicitante, fechaInicio, fechaFin);
            if (resultado != null) {
                return resultado;
            }
            // Si no hay jefe en el depto actual, pasar al siguiente nivel.
            dptoActual = getSuperior(dptoActual);
        }

        logger.warn(
                "No se pudo determinar un departamento de destino. Se retorna el departamento inicial como fallback.");
        return departamentoInicial;
    }

    private DepartamentoResponse procesarDepartamento(DepartamentoResponse dptoActual, Integer rutSolicitante,
            LocalDate fechaInicio, LocalDate fechaFin) {
        Integer rutJefe = dptoActual.getRutJefe();
        if (rutJefe == null) {
            return null;
        }

        boolean jefeAusente = isAusenteEnFecha(rutJefe, fechaInicio)
                || hasAprobacion(rutJefe, fechaInicio, fechaFin);

        if (!jefeAusente) {
            // El jefe está disponible, este es el departamento de destino.
            logger.info("Jefe de departamento {} disponible. Retornando departamento.", dptoActual.getNombre());
            return dptoActual;
        }

        // Si el jefe está ausente, buscar subrogancia.
        Optional<Subrogancia> subroganciaActiva = findActiveSubrogate(rutJefe, fechaInicio, fechaFin);

        if (subroganciaActiva.isPresent()) {
            Integer rutSubrogante = subroganciaActiva.get().getSubrogante();

            if (!rutSubrogante.equals(rutSolicitante)) {
                // Se encontró un subrogante. El destino es el departamento del subrogante.
                FuncionarioResponseApi funcionarioSubrogante = funcionarioService.getFuncionarioByRut(rutSubrogante);
                DepartamentoResponse dptoSubrogante = getDepartamentoById(funcionarioSubrogante.getCodDepto());
                logger.info("Jefe {} ausente, pero se encontró subrogante {}. Redirigiendo a departamento {}.",
                        dptoActual.getNombre(), funcionarioSubrogante.getNombreCompleto(), dptoSubrogante.getNombre());
                return dptoSubrogante;
            } else {
                logger.info("El subrogante es el mismo solicitante. Buscando en jerarquía superior.");
            }
        } else {
            // Si el jefe está ausente y no hay subrogante, continuar al superior.
            logger.info("Jefe de departamento {} no disponible y sin subrogancia. Buscando en jerarquía superior.",
                    dptoActual.getNombre());
        }
        return null;
    }

    private DepartamentoResponse getSuperior(DepartamentoResponse d) {
        DepartamentoResponse superior = null;
        if (d.getIdDeptoSuperior() != null) {
            superior = apiDepartamentoService.obtenerDepartamento(d.getIdDeptoSuperior());
        }
        return superior;
    }

    private Optional<Subrogancia> findActiveSubrogate(Integer rutJefe, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Subrogancia> subrogancias = subroganciaRepository
                .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, fechaInicio,
                        fechaFin);
        return subrogancias.stream().findFirst();
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
        logger.debug("Funcionario con rut {} tiene aprobaciones entre {} y {}: {}", rut, fechaInicio, fechaFin,
                hasAprob);
        return hasAprob;
    }

    @Override
    public List<DepartamentoList> getDepartamentos() {
        return apiDepartamentoService.getDepartamentosList();
    }

    @Override
    public void updateJefeDepartamento(Long idDepto, Integer rutJefe) {
        apiDepartamentoService.updateJefeDepartamento(idDepto, rutJefe);
    }

}
