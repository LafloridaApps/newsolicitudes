package com.newsolicitudes.newsolicitudes.service.aprobacioneslist;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.AprobacionListPage;
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

    public AprobacionListPage aprobacionListToAprobacion(Page<Aprobacion> aprobaciones) {

        return new AprobacionListPage(
                aprobaciones.getTotalPages(),
                aprobaciones.getTotalElements(),
                aprobaciones.getNumber(),
                mapToAprobacionList(aprobaciones.getContent()));

    }

    private List<AprobacionList> mapToAprobacionList(List<Aprobacion> aprobaciones) {
        return aprobaciones.stream()
                .filter(s -> s.getEstadoSolicitud().equals(EstadoSolicitud.APROBADA))
                .map(aprobacion -> AprobacionList.builder()
                        .idSolicitud(aprobacion.getIdSolicitud())
                        .rut(getRut(aprobacion.getRutSolicitud()))
                        .nombres(obtenerNombres(aprobacion.getRutSolicitud()))
                        .apellidos(obtnerApellidos(aprobacion.getRutSolicitud()))
                        .departamento(obtenerDepartamento(aprobacion.getDeptoSolicitud()))
                        .jornada(obtenerJornada(aprobacion.getSolicitud()))
                        .desde(aprobacion.getFechaInicioSolicitud().toString())
                        .hasta(aprobacion.getFechaTerminoSolicitud().toString())
                        .duracion(aprobacion.getDuracionSolicitud())
                        .fechaSolicitud(aprobacion.getFechaSolicitud().toString())
                        .tipoSolicitud(aprobacion.getTipoSolicitud())
                        .tipoContrato(getTipoContrato(aprobacion.getRutSolicitud()))
                        .url(aprobacion.getUrlPdf())
                        .build())
                .sorted(Comparator.comparing(AprobacionList::getApellidos).thenComparing(AprobacionList::getNombres))
                .toList();
    }

    private String getRut(Integer rut) {

        FuncionarioResponseApi funcionarioResponse = funcionarioService.getFuncionarioByRut(rut);

        return funcionarioResponse.getRut().toString().concat("-").concat(funcionarioResponse.getVrut());

    }

    private String obtenerNombres(Integer rut) {

        return funcionarioService.getFuncionarioByRut(rut).getNombre();

    }

    private String obtnerApellidos(Integer rut) {

        FuncionarioResponseApi funcionarioResponse = funcionarioService.getFuncionarioByRut(rut);

        return funcionarioResponse.getApellidoPaterno().concat(" ").concat(funcionarioResponse.getApellidoMaterno());

    }

    private String obtenerDepartamento(Long idDepto) {

        return departamentoService.getDepartamentoById(idDepto).getNombre();
    }

    public String obtenerJornada(Solicitud solicitud) {
        if (solicitud.getTipoSolicitud().equals(Solicitud.TipoSolicitud.ADMINISTRATIVO)) {
            // Si la duración es de más de un día, siempre es jornada completa
            if (!solicitud.getFechaInicio().isEqual(solicitud.getFechaTermino())) {
                return Solicitud.Jornada.COMPLETA.name();
            } else { // Es el mismo día
                if (solicitud.getJornadaInicio() != null) {
                    if (solicitud.getJornadaInicio().equals(Solicitud.Jornada.AM)) {
                        return Solicitud.Jornada.AM.name();
                    } else if (solicitud.getJornadaInicio().equals(Solicitud.Jornada.PM)) {
                        return Solicitud.Jornada.PM.name();
                    }
                }
                // Si es el mismo día y no es AM ni PM, o jornadaInicio es COMPLETA, se
                // considera completa por defecto
                return Solicitud.Jornada.COMPLETA.name();
            }
        } else if (solicitud.getTipoSolicitud().equals(Solicitud.TipoSolicitud.FERIADO)) {
            return Solicitud.Jornada.COMPLETA.name();
        }
        return "";
    }

    private String getTipoContrato(Integer ruInteger) {
        Integer ident = funcionarioService.getFuncionarioByRut(ruInteger).getIdent();

        return switch (ident) {
            case 1 -> {
                var funcionario = funcionarioService.getFuncionarioByRut(ruInteger);
                yield funcionario != null && funcionario.getTipoContrato() != null
                        ? funcionario.getTipoContrato().trim()
                        : "DESCONOCIDO";
            }
            default -> "HONORARIOS";
        };

    }

}
