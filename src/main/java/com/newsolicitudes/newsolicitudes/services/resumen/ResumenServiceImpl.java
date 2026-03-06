package com.newsolicitudes.newsolicitudes.services.resumen;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoSubrogadoDTO;
import com.newsolicitudes.newsolicitudes.dto.ProximaAusenciaDTO;
import com.newsolicitudes.newsolicitudes.dto.ResumenJefeDepartamentoDTO;
import com.newsolicitudes.newsolicitudes.dto.SolicitudPendienteDTO;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository; // Nuevo import
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoJerarquiaDTO;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId; // Nuevo import
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;

@Service
public class ResumenServiceImpl implements ResumenService {

    private static final Logger logger = LoggerFactory.getLogger(ResumenServiceImpl.class);

    private final SubroganciaRepository subroganciaRepository;
    private final SolicitudRepository solicitudRepository;
    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;
    private final DerivacionRepository derivacionRepository; // Nueva inyección
    public static final String DEFAULTVALUE = "Desconocido";
    public static final String TIME_ZONE = "America/Santiago";

    public ResumenServiceImpl(SubroganciaRepository subroganciaRepository,
            SolicitudRepository solicitudRepository,
            ApiExtFuncionarioService apiExtFuncionarioService,
            ApiDepartamentoService apiDepartamentoService,
            DerivacionRepository derivacionRepository) {
        this.subroganciaRepository = subroganciaRepository;
        this.solicitudRepository = solicitudRepository;
        this.apiExtFuncionarioService = apiExtFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
        this.derivacionRepository = derivacionRepository;
    }

    @Override
    public ResumenJefeDepartamentoDTO getResumenJefeDepartamento(Integer rutJefe, Long idDepartamento) {
        List<DepartamentoSubrogadoDTO> departamentosSubrogados = getDepartamentosSubrogados(rutJefe);

        LocalDate today = LocalDate.now(ZoneId.of(TIME_ZONE));
        List<Long> idDepartamentos = new ArrayList<>();
        idDepartamentos.add(idDepartamento);
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubrogante(rutJefe);
        subrogancias.stream()
                .filter(s -> !today.isBefore(s.getFechaInicio()) && !today.isAfter(s.getFechaFin()))
                .forEach(subrogancia -> idDepartamentos.add(subrogancia.getIdDepto()));

        List<SolicitudPendienteDTO> solicitudesPendientes = getSolicitudesPendientes(idDepartamentos);
        List<ProximaAusenciaDTO> proximasAusencias = getProximasAusencias(idDepartamento);
        Integer ausenciasHoy = getAusenciasEquipoHoy(idDepartamento); // Nueva llamada

        return new ResumenJefeDepartamentoDTO(departamentosSubrogados, solicitudesPendientes, proximasAusencias,
                ausenciasHoy);
    }

    private List<DepartamentoSubrogadoDTO> getDepartamentosSubrogados(Integer rutJefe) {
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubrogante(rutJefe);
        LocalDate today = LocalDate.now(ZoneId.of(TIME_ZONE));

        return subrogancias.stream()
                .filter(s -> !today.isBefore(s.getFechaInicio()) && !today.isAfter(s.getFechaFin()))
                .map(subrogancia -> {
                    String nombreDepartamento = DEFAULTVALUE;
                    try {
                        DepartamentoResponse departamento = apiDepartamentoService
                                .obtenerDepartamento(subrogancia.getIdDepto());
                        if (departamento != null) {
                            nombreDepartamento = departamento.getNombre();
                        }
                    } catch (Exception e) {
                        logger.error("Error al obtener departamento para subrogancia {}: {}", subrogancia.getIdDepto(),
                                e.getMessage());
                    }
                    return new DepartamentoSubrogadoDTO(nombreDepartamento, subrogancia.getFechaInicio(),
                            subrogancia.getFechaFin());
                })
                .toList();
    }

    private List<SolicitudPendienteDTO> getSolicitudesPendientes(List<Long> idDepartamentos) {
        Map<Long, Solicitud> solicitudesMap = new HashMap<>();

        // 1. Get requests derived to the user's departments that are pending
        List<Long> idsDeSolicitudesDerivadas = derivacionRepository
                .findSolicitudIdsByDeptoIdsAndEstadoPendiente(idDepartamentos);
        if (!idsDeSolicitudesDerivadas.isEmpty()) {
            // We use findAllById as the previous query already filtered by PENDIENTE state
            List<Solicitud> solicitudesDerivadas = solicitudRepository.findAllById(idsDeSolicitudesDerivadas);
            solicitudesDerivadas.forEach(s -> solicitudesMap.put(s.getId(), s));
        }

        // 2. Get pending requests with no derivations and check their original
        // department
        List<Solicitud> solicitudesSinDerivacion = solicitudRepository
                .findByEstadoAndDerivacionesIsEmpty(Solicitud.EstadoSolicitud.PENDIENTE);
        for (Solicitud solicitud : solicitudesSinDerivacion) {
            if (solicitudesMap.containsKey(solicitud.getId())) {
                continue; // Already processed from the derived list
            }
            try {
                FuncionarioResponseApi funcionario = apiExtFuncionarioService
                        .obtenerDetalleColaborador(solicitud.getRut());
                if (funcionario != null && idDepartamentos.contains(funcionario.getCodDepto())) {
                    solicitudesMap.put(solicitud.getId(), solicitud);
                }
            } catch (Exception e) {
                logger.error("Error al obtener el funcionario para el rut: {}. Error: {}", solicitud.getRut(),
                        e.getMessage());
            }
        }

        // 3. Convert the unique solicitations to DTOs
        return solicitudesMap.values().stream()
                .map(solicitud -> {
                    String nombreFuncionario = DEFAULTVALUE;
                    try {
                        FuncionarioResponseApi funcionario = apiExtFuncionarioService
                                .obtenerDetalleColaborador(solicitud.getRut());
                        if (funcionario != null) {
                            nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno();
                        }
                    } catch (Exception e) {
                        logger.error("Error al obtener funcionario para solicitud {}: {}", solicitud.getRut(),
                                e.getMessage());
                    }
                    return new SolicitudPendienteDTO(nombreFuncionario, solicitud.getTipoSolicitud().name());
                })
                .toList();
    }

    private List<ProximaAusenciaDTO> getProximasAusencias(Long idDepartamento) {
        LocalDate today = FechaUtils.fechaActual();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        DepartamentoJerarquiaDTO deptoJerarquia = getJerarquitaPorIdDepartamento(idDepartamento);
        Set<Long> deptoIds = new HashSet<>();
        collectDeptoIdsRecursivo(deptoJerarquia, deptoIds);

        List<Solicitud> proximas = solicitudRepository.findAusenciasMes(
                List.of("APROBADA", "DECRETADA"), deptoIds, tomorrow, endOfMonth);

        return mapListToDtoList(proximas, endOfMonth);

    }

    private Integer getAusenciasEquipoHoy(Long idDepartamento) {
        LocalDate todayStgo = LocalDate.now(ZoneId.of(TIME_ZONE));

        DepartamentoJerarquiaDTO deptoJerarquia = getJerarquitaPorIdDepartamento(idDepartamento);
        Set<Long> deptoIds = new HashSet<>();
        collectDeptoIdsRecursivo(deptoJerarquia, deptoIds);

        return solicitudRepository.findAusenciaToday(
                List.of("APROBADA", "DECRETADA"),
                deptoIds, todayStgo).size();

    }

    private void collectDeptoIdsRecursivo(DepartamentoJerarquiaDTO depto, Set<Long> ids) {
        if (depto == null)
            return;
        ids.add(depto.getId());
        if (depto.getHijos() != null) {
            for (DepartamentoJerarquiaDTO hijo : depto.getHijos()) {
                collectDeptoIdsRecursivo(hijo, ids);
            }
        }
    }

    private DepartamentoJerarquiaDTO getJerarquitaPorIdDepartamento(Long id) {

        return apiDepartamentoService.getJerarquiaPorId(id);

    }

    private List<ProximaAusenciaDTO> mapListToDtoList(List<Solicitud> solicitudes, LocalDate endOfMonth) {
        return solicitudes.stream().map(solicitud -> {

            String nombreFuncionario = DEFAULTVALUE;
            try {
                FuncionarioResponseApi funcionario = apiExtFuncionarioService
                        .obtenerDetalleColaborador(solicitud.getRut());
                if (funcionario != null) {
                    nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno();
                }
            } catch (Exception e) {
                logger.error("Error al obtener funcionario para proxima ausencia {}: {}", solicitud.getRut(),
                        e.getMessage());
            }

            return new ProximaAusenciaDTO(nombreFuncionario, endOfMonth);

        }).collect(Collectors.groupingBy(ProximaAusenciaDTO::getNombreFuncionario))
                .entrySet().stream()
                .map(entry -> {
                    String nombre = entry.getKey();

                    return new ProximaAusenciaDTO(nombre, endOfMonth);

                }).sorted(Comparator.comparing(ProximaAusenciaDTO::getNombreFuncionario))
                .toList();

    }
}
