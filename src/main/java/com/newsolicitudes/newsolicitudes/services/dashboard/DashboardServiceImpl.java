package com.newsolicitudes.newsolicitudes.services.dashboard;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciaDto;
import com.newsolicitudes.newsolicitudes.dto.DashboardResponseDto;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoDropdownDto;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoJerarquiaDTO;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoMetricaDto;
import com.newsolicitudes.newsolicitudes.dto.KpisDto;
import com.newsolicitudes.newsolicitudes.dto.MesMetricaDto;
import com.newsolicitudes.newsolicitudes.dto.PeriodoDto;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final ApiDepartamentoService apiDepartamentoService;
    private final SolicitudRepository solicitudRepository;
    private final AprobacionRepository aprobacionRepository;
    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private static final int MONTH_INICIO = 1;
    private static final int MONTH_FIN= 12;

    // Colores de Bootstrap para asignar secuencialmente a los departamentos en los
    // gráficos
    private static final List<String> COLORES_BOOTSTRAP = Arrays.asList(
            "bg-primary", "bg-info", "bg-success", "bg-warning", "bg-danger", "bg-secondary", "bg-dark");

    public DashboardServiceImpl(ApiDepartamentoService apiDepartamentoService,
            SolicitudRepository solicitudRepository,
            AprobacionRepository aprobacionRepository,
            ApiExtFuncionarioService apiExtFuncionarioService) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.solicitudRepository = solicitudRepository;
        this.aprobacionRepository = aprobacionRepository;
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    public DashboardResponseDto obtenerResumenDashboard(Long codDeptoUsuario, Integer anio, Long codDeptoFiltro) {
        logger.info("Iniciando obtención de dashboard para usuario del depto {} en el año {}", codDeptoUsuario, anio);

        // 1. Identificar la Dirección (Directorate) del usuario subiendo en la
        // jerarquía
        DepartamentoResponse direccion = obtenerDireccionSuperior(codDeptoUsuario);

        if (direccion == null) {
            throw new IllegalArgumentException(
                    "No se pudo determinar la Dirección para el departamento del usuario: " + codDeptoUsuario);
        }

        // 2. Obtener toda la familia de departamentos (La Dirección y todos sus
        // sub-departamentos)
        List<DepartamentoResponse> familiaDepartamentos = apiDepartamentoService.obtenerFamiliaDepto(direccion.getId());

        // 3. Preparar la lista de IDs de departamentos a consultar (aplicando filtro
        // del dropdown si existe)
        List<Long> idsDepartamentosAConsultar;
        if (codDeptoFiltro != null) {
            idsDepartamentosAConsultar = List.of(codDeptoFiltro);
        } else {
            idsDepartamentosAConsultar = familiaDepartamentos.stream()
                    .map(DepartamentoResponse::getId)
                    .toList();
        }

        // 4. Obtener las solicitudes de la base de datos dentro del año indicado
        LocalDate inicioAnio = LocalDate.of(anio, MONTH_INICIO, 1);
        LocalDate finAnio = LocalDate.of(anio, MONTH_FIN, 31);
        List<Solicitud> solicitudes = solicitudRepository
                .findByIdDeptoInAndFechaInicioBetween(idsDepartamentosAConsultar, inicioAnio, finAnio);

        // 5. Procesar los datos y ensamblar la respuesta
        DashboardResponseDto response = new DashboardResponseDto();
        response.setKpis(calcularKpis(solicitudes));
        response.setPorMes(calcularMetricasPorMes(solicitudes));
        response.setPorDepartamento(calcularMetricasPorDepartamento(solicitudes, familiaDepartamentos));
        response.setDepartamentosDropdown(construirDropdownDepartamentos(familiaDepartamentos));

        return response;
    }

    /**
     * Navega hacia arriba en la jerarquía hasta encontrar el departamento de nivel
     * "DIRECCION".
     * Si no se encuentra, retorna el departamento de mayor nivel posible.
     */
    private DepartamentoResponse obtenerDireccionSuperior(Long codDepto) {
        DepartamentoResponse deptoActual = apiDepartamentoService.obtenerDepartamento(codDepto);

        while (deptoActual != null) {
            if ("DIRECCION".equalsIgnoreCase(deptoActual.getNivelDepartamento())) {
                return deptoActual;
            }
            if (deptoActual.getIdDeptoSuperior() != null) {
                deptoActual = apiDepartamentoService.obtenerDepartamento(deptoActual.getIdDeptoSuperior());
            } else {
                break; // Llegó al tope sin ser explícitamente "DIRECCION"
            }
        }
        return deptoActual;
    }

    /**
     * Calcula los KPIs generales del año.
     */
    private KpisDto calcularKpis(List<Solicitud> solicitudes) {
        KpisDto kpis = new KpisDto();
        kpis.setTotalAnual(solicitudes.size());

        long aprobadas = 0;
        long pendientes = 0;
        long rechazadas = 0;
        long postergadas = 0;

        for (Solicitud solicitud : solicitudes) {
            EstadoSolicitud estado = solicitud.getEstado();
            if (estado == EstadoSolicitud.APROBADA || 
                "DECRETADA".equals(estado.name()) || 
                "FORMULARIO_EN_RRHH".equals(estado.name())) {
                aprobadas++;
            } else if (estado == EstadoSolicitud.ANULADA || "RECHAZADA".equals(estado.name())) {
                // Se considera anulada/rechazada como parte del rechazo en el KPI
                rechazadas++;
            } else if ("POSTERGADA".equals(estado.name())) {
                postergadas++;
            } else {
                // Cualquier otro estado intermedio (PENDIENTE, VISADA, etc.) se suma aquí
                pendientes++;
            }
        }

        kpis.setAprobadas(aprobadas);
        kpis.setPendientes(pendientes);
        kpis.setRechazadas(rechazadas);
        kpis.setPostergadas(postergadas);
        return kpis;
    }

    /**
     * Agrupa la cantidad de solicitudes por mes, iterando del Enero a Diciembre.
     */
    private List<MesMetricaDto> calcularMetricasPorMes(List<Solicitud> solicitudes) {
        Map<Month, Long> conteoPorMes = solicitudes.stream()
                .filter(s -> s.getFechaInicio() != null)
                .collect(Collectors.groupingBy(s -> s.getFechaInicio().getMonth(), Collectors.counting()));

        List<MesMetricaDto> metricasMes = new ArrayList<>();
        for (Month mes : Month.values()) {
            String nombreMes = mes.getDisplayName(TextStyle.SHORT, Locale.of("es", "ES"));
            // Capitaliza (ej. 'Ene' en lugar de 'ene')
            nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);

            long cantidad = conteoPorMes.getOrDefault(mes, 0L);
            metricasMes.add(new MesMetricaDto(nombreMes, cantidad));
        }
        return metricasMes;
    }

    /**
     * Agrupa la cantidad de solicitudes por sub-departamento, asignando un color
     * base.
     */
    private List<DepartamentoMetricaDto> calcularMetricasPorDepartamento(List<Solicitud> solicitudes,
            List<DepartamentoResponse> familiaDepartamentos) {
        Map<Long, String> mapaNombresDepto = familiaDepartamentos.stream()
                .collect(
                        Collectors.toMap(DepartamentoResponse::getId, DepartamentoResponse::getNombre, (n1, n2) -> n1));

        Map<Long, Long> conteoPorDepto = solicitudes.stream()
                .filter(s -> s.getIdDepto() != null)
                .collect(Collectors.groupingBy(Solicitud::getIdDepto, Collectors.counting()));

        List<DepartamentoMetricaDto> metricasDepto = new ArrayList<>();
        int colorIndex = 0;

        // Se ordenan descendentemente para mostrar los departamentos con más permisos
        // en primer lugar
        List<Map.Entry<Long, Long>> entradasOrdenadas = conteoPorDepto.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .toList();

        for (Map.Entry<Long, Long> entry : entradasOrdenadas) {
            String nombreDepto = mapaNombresDepto.getOrDefault(entry.getKey(), "Departamento " + entry.getKey());
            String color = COLORES_BOOTSTRAP.get(colorIndex % COLORES_BOOTSTRAP.size());

            metricasDepto.add(new DepartamentoMetricaDto(nombreDepto, entry.getValue(), color));
            colorIndex++;
        }
        return metricasDepto;
    }

    /**
     * Construye la lista necesaria para el componente desplegable de filtrado.
     */
    private List<DepartamentoDropdownDto> construirDropdownDepartamentos(
            List<DepartamentoResponse> familiaDepartamentos) {
        List<DepartamentoDropdownDto> dropdown = new ArrayList<>();

        // Opción predeterminada
        dropdown.add(new DepartamentoDropdownDto("todos", "Todos"));

        // Se incluyen todos los departamentos de la Dirección actual
        for (DepartamentoResponse depto : familiaDepartamentos) {
            dropdown.add(new DepartamentoDropdownDto(String.valueOf(depto.getId()), depto.getNombre()));
        }
        return dropdown;
    }

    @Override
    public List<DashboardAusenciaDto> getAusenciasPorDepartamento(Long departamentoId, LocalDate fecha) {
        DepartamentoJerarquiaDTO deptoJerarquia = apiDepartamentoService.getJerarquiaPorId(departamentoId);

        LocalDate primerDia = primerDiaDelMes(fecha);
        LocalDate ultimoDia = ultimoDiaDelMes(fecha);

        Map<Long, String> deptoNombres = new HashMap<>();
        Set<Long> deptoIds = new HashSet<>();
        collectDeptoInfo(deptoJerarquia, deptoIds, deptoNombres);

        List<Solicitud> solicitudes = solicitudRepository.findAusenciasMes(
                List.of("APROBADA", "DECRETADA"), deptoIds, primerDia, ultimoDia);

        return solicitudes.stream().map(solicitud -> mapToDashboardDto(solicitud, deptoNombres))
                .toList();
    }

    private void collectDeptoInfo(DepartamentoJerarquiaDTO depto, Set<Long> ids, Map<Long, String> nombres) {
        if (depto != null) {
            ids.add(depto.getId());
            nombres.put(depto.getId(), depto.getNombre());
            if (depto.getHijos() != null) {
                for (DepartamentoJerarquiaDTO hijo : depto.getHijos()) {
                    collectDeptoInfo(hijo, ids, nombres);
                }
            }
        }
    }

    private DashboardAusenciaDto mapToDashboardDto(Solicitud solicitud, Map<Long, String> deptoNombres) {
        Optional<Aprobacion> aprobacionOpt = aprobacionRepository.findBySolicitud(solicitud);
        FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());

        String nombreGrupo = deptoNombres.getOrDefault(solicitud.getIdDepto(), "");
        String nombreFuncionario = "";
        String rutFuncionario = "";

        if (funcionario != null) {
            nombreFuncionario = funcionario.getNombre() + " " + funcionario.getApellidoPaterno() + " "
                    + funcionario.getApellidoMaterno();
            // The RUT from the API might need formatting (e.g., adding dots and hyphen)
            rutFuncionario = funcionario.getRut().toString();
        }

        return new DashboardAusenciaDto(
                nombreGrupo,
                nombreFuncionario,
                rutFuncionario,
                solicitud.getTipoSolicitud().name(),
                String.valueOf(solicitud.getId()),
                aprobacionOpt.map(Aprobacion::getFechaAprobacion).orElse(null),
                new PeriodoDto(solicitud.getFechaInicio(), solicitud.getFechaTermino()));
    }

    private LocalDate primerDiaDelMes(LocalDate fecha) {
        return fecha.withDayOfMonth(1);
    }

    private LocalDate ultimoDiaDelMes(LocalDate fecha) {
        return fecha.withDayOfMonth(fecha.lengthOfMonth());
    }
}