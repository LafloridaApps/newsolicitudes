package com.newsolicitudes.newsolicitudes.services.dashboard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DashboardAusenciaDto;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoJerarquiaDTO;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
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

    private final ApiDepartamentoService apiDepartamentoService;
    private final SolicitudRepository solicitudRepository;
    private final AprobacionRepository aprobacionRepository;
    private final ApiExtFuncionarioService apiExtFuncionarioService;

    public DashboardServiceImpl(ApiDepartamentoService apiDepartamentoService, SolicitudRepository solicitudRepository,
            AprobacionRepository aprobacionRepository, ApiExtFuncionarioService apiExtFuncionarioService) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.solicitudRepository = solicitudRepository;
        this.aprobacionRepository = aprobacionRepository;
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    public List<DashboardAusenciaDto> getAusenciasPorDepartamento(Long departamentoId, LocalDate fecha) {
        DepartamentoJerarquiaDTO deptoJerarquia = apiDepartamentoService.getJerarquiaPorId(departamentoId);

        LocalDate primerDia = primerDiaDelMes(fecha);
        LocalDate ultimoDia = ultimoDiaDelMes(fecha);


        Map<Long, String> deptoNombres = new HashMap<>();
        List<Long> deptoIds = new ArrayList<>();
        collectDeptoInfo(deptoJerarquia, deptoIds, deptoNombres);

        List<Solicitud> solicitudes = solicitudRepository.findByEstadoInAndIdDeptoInAndFechaInicioGreaterThanEqualAndFechaTerminoLessThanEqual(
                List.of(EstadoSolicitud.APROBADA, EstadoSolicitud.DECRETADA), deptoIds, primerDia, ultimoDia);

        return solicitudes.stream().map(solicitud -> mapToDashboardDto(solicitud, deptoNombres))
                .toList();
    }

    private void collectDeptoInfo(DepartamentoJerarquiaDTO depto, List<Long> ids, Map<Long, String> nombres) {
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

    private  LocalDate primerDiaDelMes(LocalDate fecha) {
        return fecha.withDayOfMonth(1);
    }

    
    private  LocalDate ultimoDiaDelMes(LocalDate fecha) {
        return fecha.withDayOfMonth(fecha.lengthOfMonth());
    }


}
