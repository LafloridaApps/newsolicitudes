package com.newsolicitudes.newsolicitudes.services.mapper;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;

@Component
public class SolicitudMapper {

    private final ApiFuncionarioService apiFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;

    public SolicitudMapper(ApiFuncionarioService apiFuncionarioService, ApiDepartamentoService apiDepartamentoService) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    public SolicitudDto solicitudDtoMapper(Solicitud solicitud) {
        SolicitudDto dto = new SolicitudDto();
        dto.setId(solicitud.getId());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud().toString());
        dto.setFechaInicio(solicitud.getFechaInicio().toString());
        dto.setFechaFin(solicitud.getFechaTermino().toString());
        dto.setJornadaInicio(solicitud.getJornadaInicio() != null ? solicitud.getJornadaInicio().name() : "");
        dto.setJornadaFin(solicitud.getJornadaTermino() != null ? solicitud.getJornadaTermino().name() : "");
        dto.setTipoSolicitud(solicitud.getTipoSolicitud().name());
        dto.setDepartamentoOrigen(String.valueOf(solicitud.getIdDepto()));
        dto.setEstadoSolicitud(solicitud.getEstado().name());
        dto.setCantidadDias(solicitud.getCantidadDias());

        // Obtener nombre del funcionario
        FuncionarioResponse funcionario = apiFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
        if (funcionario != null) {
            dto.setNombreFuncionario(funcionario.getNombre() + " " + funcionario.getApellidoPaterno() + " "
                    + funcionario.getApellidoMaterno());
            dto.setRutSolicitante(funcionario.getRut() + "-" + funcionario.getVrut());
        }

        // Obtener nombre del departamento
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());
        if (departamento != null) {
            dto.setNombreDepartamento(departamento.getNombre());
        }

        return dto;
    }

    public Solicitud mapToSolicitud(SolicitudRequest request, Integer solicitante, Long idDepto) {
        Solicitud solicitud = new Solicitud();
        solicitud.setCantidadDias(request.getDiasUsar());
        solicitud.setFechaSolicitud(request.getFechaSolicitud());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaTermino(request.getFechaFin());
        solicitud.setIdDepto(idDepto);
        solicitud.setRut(solicitante);

        solicitud.setTipoSolicitud(Solicitud.TipoSolicitud.valueOf(request.getTipoSolicitud()));
        solicitud.setEstado(Solicitud.EstadoSolicitud.PENDIENTE);
        solicitud.setJornadaInicio(
                request.getJornadaInicio() != null ? Solicitud.Jornada.valueOf(request.getJornadaInicio()) : null);
        solicitud.setJornadaTermino(
                request.getJornadaTermino() != null ? Solicitud.Jornada.valueOf(request.getJornadaTermino()) : null);

        return solicitud;
    }

}
