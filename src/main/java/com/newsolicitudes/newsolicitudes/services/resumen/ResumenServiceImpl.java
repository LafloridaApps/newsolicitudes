package com.newsolicitudes.newsolicitudes.services.resumen;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoSubrogadoDTO;
import com.newsolicitudes.newsolicitudes.dto.ProximaAusenciaDTO;
import com.newsolicitudes.newsolicitudes.dto.ResumenJefeDepartamentoDTO;
import com.newsolicitudes.newsolicitudes.dto.SolicitudPendienteDTO;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.entities.Derivacion; // Nuevo import
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository; // Nuevo import
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId; // Nuevo import
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class ResumenServiceImpl implements ResumenService {

    private static final Logger logger = LoggerFactory.getLogger(ResumenServiceImpl.class);

    private final SubroganciaRepository subroganciaRepository;
    private final SolicitudRepository solicitudRepository;
    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;
    private final DerivacionRepository derivacionRepository; // Nueva inyección
    public  static final String DEFAULTVALUE="Desconocido";

    public ResumenServiceImpl(SubroganciaRepository subroganciaRepository,
                              SolicitudRepository solicitudRepository,
                              ApiExtFuncionarioService apiExtFuncionarioService,
                              ApiDepartamentoService apiDepartamentoService,
                              DerivacionRepository derivacionRepository) {
        this.subroganciaRepository = subroganciaRepository;
        this.solicitudRepository = solicitudRepository;
        this.apiExtFuncionarioService = apiExtFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
        this.derivacionRepository = derivacionRepository; // Asignar nueva inyección
    }

    @Override
    public ResumenJefeDepartamentoDTO getResumenJefeDepartamento(Integer rutJefe, Long idDepartamento) {
        logger.info("Iniciando getResumenJefeDepartamento para rutJefe: {}, idDepartamento: {}", rutJefe, idDepartamento);
        List<DepartamentoSubrogadoDTO> departamentosSubrogados = getDepartamentosSubrogados(rutJefe);
        List<SolicitudPendienteDTO> solicitudesPendientes = getSolicitudesPendientes(idDepartamento);
        List<ProximaAusenciaDTO> proximasAusencias = getProximasAusencias(idDepartamento);
        Integer ausenciasHoy = getAusenciasEquipoHoy(idDepartamento); // Nueva llamada

        return new ResumenJefeDepartamentoDTO(departamentosSubrogados, solicitudesPendientes, proximasAusencias, ausenciasHoy);
    }

    private List<DepartamentoSubrogadoDTO> getDepartamentosSubrogados(Integer rutJefe) {
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubrogante(rutJefe);
        return subrogancias.stream()
                .map(subrogancia -> {
                    String nombreDepartamento =DEFAULTVALUE;
                    try {
                        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(subrogancia.getIdDepto());
                        if (departamento != null) {
                            nombreDepartamento = departamento.getNombre();
                        }
                    } catch (Exception e) {
                        logger.error("Error al obtener departamento para subrogancia {}: {}", subrogancia.getIdDepto(), e.getMessage());
                    }
                    return new DepartamentoSubrogadoDTO(nombreDepartamento, subrogancia.getFechaInicio(), subrogancia.getFechaFin());
                })
                .toList();
    }

    private List<SolicitudPendienteDTO> getSolicitudesPendientes(Long idDepartamentoActual) {
        logger.info("Obteniendo solicitudes pendientes para idDepartamentoActual: {}", idDepartamentoActual);
        List<Solicitud> todasLasSolicitudesPendientes = solicitudRepository.findByEstado(Solicitud.EstadoSolicitud.PENDIENTE);
        logger.debug("Total de solicitudes en estado PENDIENTE: {}", todasLasSolicitudesPendientes.size());
        List<SolicitudPendienteDTO> pendientes = new ArrayList<>();

        for (Solicitud solicitud : todasLasSolicitudesPendientes) {
            logger.debug("Evaluando solicitud ID: {}, Estado: {}, idDepto: {}", solicitud.getId(), solicitud.getEstado(), solicitud.getIdDepto());
            if (isSolicitudPendingForDepartment(solicitud, idDepartamentoActual)) {
                String nombreFuncionario = DEFAULTVALUE;
                try {
                    FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
                    if (funcionario != null) {
                        nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno();
                    }
                } catch (Exception e) {
                    logger.error("Error al obtener funcionario para solicitud {}: {}", solicitud.getRut(), e.getMessage());
                }
                pendientes.add(new SolicitudPendienteDTO(nombreFuncionario, solicitud.getTipoSolicitud().name()));
                logger.debug("Solicitud ID: {} agregada a la lista de pendientes.", solicitud.getId());
            }
        }
        logger.info("Total de solicitudes pendientes para el departamento {}: {}", idDepartamentoActual, pendientes.size());
        return pendientes;
    }

    private boolean isSolicitudPendingForDepartment(Solicitud solicitud, Long idDepartamentoActual) {
        logger.debug("Verificando si solicitud ID: {} es pendiente para departamento: {}", solicitud.getId(), idDepartamentoActual);

        // Case 1: Solicitud has no derivations at all
        List<Derivacion> allDerivaciones = derivacionRepository.findBySolicitudIdOrderByFechaDerivacionDesc(solicitud.getId());
        if (allDerivaciones.isEmpty()) {
            boolean isPending = solicitud.getIdDepto().equals(idDepartamentoActual);
            logger.debug("Solicitud ID: {} sin derivaciones. Coincide idDepto ({} vs {}): {}", solicitud.getId(), solicitud.getIdDepto(), idDepartamentoActual, isPending);
            return isPending;
        }

        // Case 2: Solicitud has derivations. Find the latest derivation to idDepartamentoActual.
        Optional<Derivacion> ultimaDerivacionParaDeptoActualOpt = derivacionRepository.findTopBySolicitudIdAndIdDeptoOrderByFechaDerivacionDesc(solicitud.getId(), idDepartamentoActual);

        if (ultimaDerivacionParaDeptoActualOpt.isPresent()) {
            Derivacion ultimaDerivacionParaDeptoActual = ultimaDerivacionParaDeptoActualOpt.get();
            boolean isPending = ultimaDerivacionParaDeptoActual.getEntrada() == null;
            logger.debug("Ultima derivacion para solicitud ID: {} a departamento {}: Entrada: {}. Es pendiente: {}", solicitud.getId(), idDepartamentoActual, ultimaDerivacionParaDeptoActual.getEntrada(), isPending);
            return isPending;
        } else {
            // Case 3: Solicitud has derivations, but none of them are to idDepartamentoActual.
            // In this case, it's not pending for idDepartamentoActual.
            logger.debug("Solicitud ID: {} tiene derivaciones, pero ninguna a departamento {}. No es pendiente para este departamento.", solicitud.getId(), idDepartamentoActual);
            return false;
        }
    }

    private List<ProximaAusenciaDTO> getProximasAusencias(Long idDepartamento) {
        LocalDate today = FechaUtils.fechaActual();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        List<Solicitud> proximas = solicitudRepository.findByEstadoAndIdDeptoAndFechaInicioBetween(Solicitud.EstadoSolicitud.APROBADA, idDepartamento, tomorrow, endOfMonth);
        List<Solicitud> solicitudesQueTerminanEnElMes = solicitudRepository.findByEstadoAndIdDeptoAndFechaTerminoBetween(Solicitud.EstadoSolicitud.APROBADA, idDepartamento, tomorrow, endOfMonth);
        proximas.addAll(solicitudesQueTerminanEnElMes);

        Map<String, ProximaAusenciaDTO> proximasAusenciasMap = new HashMap<>();

        for (Solicitud solicitud : proximas) {
            String nombreFuncionario = DEFAULTVALUE;
            try {
                FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
                if (funcionario != null) {
                    nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno();
                }
            } catch (Exception e) {
                logger.error("Error al obtener funcionario para proxima ausencia {}: {}", solicitud.getRut(), e.getMessage());
            }

            if (proximasAusenciasMap.containsKey(nombreFuncionario)) {
                ProximaAusenciaDTO existing = proximasAusenciasMap.get(nombreFuncionario);
                if (solicitud.getFechaInicio().isBefore(existing.getFechaAusencia())) {
                    proximasAusenciasMap.put(nombreFuncionario, new ProximaAusenciaDTO(nombreFuncionario, solicitud.getFechaInicio()));
                }
            } else {
                proximasAusenciasMap.put(nombreFuncionario, new ProximaAusenciaDTO(nombreFuncionario, solicitud.getFechaInicio()));
            }
        }

        return new ArrayList<>(proximasAusenciasMap.values());
    }

    private Integer getAusenciasEquipoHoy(Long idDepartamento) {
        LocalDate todayStgo = LocalDate.now(ZoneId.of("America/Santiago"));

        List<Solicitud> ausenciasHoy = solicitudRepository.findByEstadoInAndIdDeptoAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
            List.of(Solicitud.EstadoSolicitud.APROBADA, Solicitud.EstadoSolicitud.DECRETADA),
            idDepartamento,
            todayStgo,
            todayStgo
        );

        return ausenciasHoy.size();
    }
}