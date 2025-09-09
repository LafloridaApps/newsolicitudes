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

import java.time.LocalDate;
import java.time.ZoneId; // Nuevo import
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ResumenServiceImpl implements ResumenService {

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
        List<DepartamentoSubrogadoDTO> departamentosSubrogados = getDepartamentosSubrogados(rutJefe);
        List<SolicitudPendienteDTO> solicitudesPendientes = getSolicitudesPendientes(idDepartamento);
        List<ProximaAusenciaDTO> proximasAusencias = getProximasAusencias(idDepartamento);
        Integer ausenciasHoy = getAusenciasEquipoHoy(idDepartamento); // Nueva llamada

        return new ResumenJefeDepartamentoDTO(departamentosSubrogados, solicitudesPendientes, proximasAusencias, ausenciasHoy); // Modificado aquí
    }

    private List<DepartamentoSubrogadoDTO> getDepartamentosSubrogados(Integer rutJefe) {
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubrogante(rutJefe); // Corregido: findBySubrogante
        return subrogancias.stream()
                .map(subrogancia -> {
                    String nombreDepartamento =DEFAULTVALUE;
                    try {
                        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(subrogancia.getIdDepto()); // Corregido: obtenerDepartamento
                        if (departamento != null) {
                            nombreDepartamento = departamento.getNombre();
                        }
                    } catch (Exception e) {
                        // Log error or handle appropriately
                    }
                    return new DepartamentoSubrogadoDTO(nombreDepartamento, subrogancia.getFechaInicio(), subrogancia.getFechaFin());
                })
                .toList();
    }

    private List<SolicitudPendienteDTO> getSolicitudesPendientes(Long idDepartamentoActual) {
        List<Solicitud> todasLasSolicitudesPendientes = solicitudRepository.findByEstado(Solicitud.EstadoSolicitud.PENDIENTE);
        List<SolicitudPendienteDTO> pendientes = new ArrayList<>();

        for (Solicitud solicitud : todasLasSolicitudesPendientes) {
            if (isSolicitudPendingForDepartment(solicitud, idDepartamentoActual)) {
                String nombreFuncionario = DEFAULTVALUE;
                try {
                    FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
                    if (funcionario != null) {
                        nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno();
                    }
                } catch (Exception e) {
                    // Log error or handle appropriately
                }
                pendientes.add(new SolicitudPendienteDTO(nombreFuncionario, solicitud.getTipoSolicitud().name()));
            }
        }
        return pendientes;
    }

    private boolean isSolicitudPendingForDepartment(Solicitud solicitud, Long idDepartamentoActual) {
        List<Derivacion> derivaciones = derivacionRepository.findBySolicitudIdOrderByFechaDerivacionDesc(solicitud.getId());

        if (derivaciones.isEmpty()) {
            // Si no tiene derivaciones, es pendiente para el departamento actual si el idDepto de la solicitud coincide
            return solicitud.getIdDepto().equals(idDepartamentoActual);
        } else {
            // Si tiene derivaciones, verificar la última derivación
            Derivacion ultimaDerivacion = derivaciones.get(0); // La primera es la última por el orden descendente

            // Es pendiente para el departamento actual si la última derivación es hacia este departamento
            // Y no tiene una entrada asociada a esa derivación
            return ultimaDerivacion.getIdDepto().equals(idDepartamentoActual) && ultimaDerivacion.getEntrada() == null;
        }
    }

    private List<ProximaAusenciaDTO> getProximasAusencias(Long idDepartamento) {
        LocalDate today = FechaUtils.fechaActual();
        LocalDate tomorrow = today.plusDays(1); // Fecha de inicio debe ser mayor a la actual
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Obtener solicitudes aprobadas cuya fecha de inicio sea a partir de mañana y hasta fin de mes
        List<Solicitud> proximas = solicitudRepository.findByEstadoAndIdDeptoAndFechaInicioBetween(Solicitud.EstadoSolicitud.APROBADA, idDepartamento, tomorrow, endOfMonth);
        // También considerar solicitudes que terminan en el mes actual pero empezaron antes
        List<Solicitud> solicitudesQueTerminanEnElMes = solicitudRepository.findByEstadoAndIdDeptoAndFechaTerminoBetween(Solicitud.EstadoSolicitud.APROBADA, idDepartamento, tomorrow, endOfMonth);
        proximas.addAll(solicitudesQueTerminanEnElMes);

        // Usar un mapa para agrupar por nombre de funcionario y obtener la próxima ausencia más cercana
        Map<String, ProximaAusenciaDTO> proximasAusenciasMap = new HashMap<>();

        for (Solicitud solicitud : proximas) {
            String nombreFuncionario = DEFAULTVALUE;
            try {
                FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
                if (funcionario != null) {
                    nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno();
                }
            } catch (Exception e) {
                // Log error or handle appropriately
            }

            // Si el funcionario ya está en el mapa, verificar si esta ausencia es más cercana
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
    } // Cierre correcto de getProximasAusencias

    private Integer getAusenciasEquipoHoy(Long idDepartamento) {
        // Obtener la fecha actual en la zona horaria de Santiago
        LocalDate todayStgo = LocalDate.now(ZoneId.of("America/Santiago"));

        // Buscar solicitudes aprobadas para el departamento y la fecha actual
        List<Solicitud> ausenciasHoy = solicitudRepository.findByEstadoInAndIdDeptoAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
            List.of(Solicitud.EstadoSolicitud.APROBADA, Solicitud.EstadoSolicitud.DECRETADA),
            idDepartamento,
            todayStgo,
            todayStgo
        );

        return ausenciasHoy.size();
    }
}