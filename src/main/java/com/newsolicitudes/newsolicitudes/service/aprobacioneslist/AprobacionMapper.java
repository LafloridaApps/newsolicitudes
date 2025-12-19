package com.newsolicitudes.newsolicitudes.service.aprobacioneslist;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

@Component
public class AprobacionMapper {

    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;

    public AprobacionMapper(FuncionarioService funcionarioService, DepartamentoService departamentoService) {
        this.funcionarioService = funcionarioService;
        this.departamentoService = departamentoService;
    }

    public List<AprobacionList> aprobacionListToAprobacion(List<Aprobacion> aprobaciones) {


        // Collect all unique RUTs and department IDs
        List<Integer> ruts = aprobaciones.stream()
                .map(Aprobacion::getRutSolicitud)
                .distinct()
                .toList();

        List<Long> deptoIds = aprobaciones.stream()
                .map(Aprobacion::getDeptoSolicitud)
                .distinct()
                .toList();

        // Fetch all required data in bulk
        Map<Integer, FuncionarioResponseApi> funcionariosMap = ruts.stream()
                .collect(Collectors.toMap(Function.identity(), funcionarioService::getFuncionarioByRut));

        Map<Long, DepartamentoResponse> departamentosMap = deptoIds.stream()
                .collect(Collectors.toMap(Function.identity(), departamentoService::getDepartamentoById));

        return mapToAprobacionList(aprobaciones, funcionariosMap, departamentosMap);
    }

    private List<AprobacionList> mapToAprobacionList(List<Aprobacion> aprobaciones,
            Map<Integer, FuncionarioResponseApi> funcionariosMap,
            Map<Long, DepartamentoResponse> departamentosMap) {
        return aprobaciones.stream()
                .filter(s -> s.getEstadoSolicitud().equals(EstadoSolicitud.APROBADA))
                .map(aprobacion -> {
                    FuncionarioResponseApi funcionario = funcionariosMap.get(aprobacion.getRutSolicitud());
                    DepartamentoResponse departamento = departamentosMap.get(aprobacion.getDeptoSolicitud());

                    return AprobacionList.builder()
                            .idSolicitud(aprobacion.getIdSolicitud())
                            .rut(getRut(funcionario))
                            .nombres(obtenerNombres(funcionario))
                            .departamento(obtenerDepartamento(departamento))
                            .jornada(obtenerJornada(aprobacion.getSolicitud()))
                            .desde(aprobacion.getFechaInicioSolicitud().toString())
                            .hasta(aprobacion.getFechaTerminoSolicitud().toString())
                            .duracion(aprobacion.getDuracionSolicitud())
                            .fechaSolicitud(aprobacion.getFechaSolicitud().toString())
                            .tipoSolicitud(aprobacion.getTipoSolicitud())
                            .tipoContrato(getTipoContrato(funcionario))
                            .url(aprobacion.getUrlPdf())
                            .build();
                })
                .sorted(Comparator.comparing(AprobacionList::getNombres))
                .toList();
    }

    private String getRut(FuncionarioResponseApi funcionario) {
        if (funcionario == null) return "";
        return funcionario.getRut().toString().concat("-").concat(funcionario.getVrut());
    }

    private String obtenerNombres(FuncionarioResponseApi funcionario) {
        if (funcionario == null) return "";
        return funcionario.getNombreCompletoReverse();
    }

    
    private String obtenerDepartamento(DepartamentoResponse departamento) {
        if (departamento == null) return "";
        return departamento.getNombre();
    }

    public String obtenerJornada(Solicitud solicitud) {
        if (solicitud.getTipoSolicitud().equals(Solicitud.TipoSolicitud.ADMINISTRATIVO)) {
            // Si es más de un día, es COMPLETA
            if (!solicitud.getFechaInicio().isEqual(solicitud.getFechaTermino())) {
                return Solicitud.Jornada.DIA.name();
            }

            // Lógica para un solo día
            Solicitud.Jornada jornadaInicio = solicitud.getJornadaInicio();
            Solicitud.Jornada jornadaTermino = solicitud.getJornadaTermino();

            // Si ambas son AM y PM, es COMPLETA
            if (jornadaInicio == Solicitud.Jornada.AM && jornadaTermino == Solicitud.Jornada.PM) {
                return Solicitud.Jornada.DIA.name();
            }

            // Si inicio es COMPLETA, es COMPLETA
            if (jornadaInicio == Solicitud.Jornada.DIA) {
                return Solicitud.Jornada.DIA.name();
            }
            
            // Si inicio es AM y termino no es PM (o es nulo), es AM
            if (jornadaInicio == Solicitud.Jornada.AM) {
                return Solicitud.Jornada.AM.name();
            }

            // Si inicio es PM, es PM
            if (jornadaInicio == Solicitud.Jornada.PM) {
                return Solicitud.Jornada.PM.name();
            }

            // Por defecto, si no hay jornada de inicio, es COMPLETA
            return Solicitud.Jornada.DIA.name();

        } else if (solicitud.getTipoSolicitud().equals(Solicitud.TipoSolicitud.FERIADO)) {
            return Solicitud.Jornada.DIA.name();
        }
        return "";
    }

    private String getTipoContrato(FuncionarioResponseApi funcionario) {
        if (funcionario == null) return "DESCONOCIDO";

        Integer ident = funcionario.getIdent();
        return switch (ident) {
            case 1 -> funcionario.getTipoContrato() != null
                        ? funcionario.getTipoContrato().trim()
                        : "DESCONOCIDO";
            default -> "HONORARIOS";
        };
    }
}