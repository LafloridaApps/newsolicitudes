package com.newsolicitudes.newsolicitudes.services.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

@Component
public class SolicitudMapper {

    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;
    private final AprobacionRepository aprobacionRepository;

    public SolicitudMapper(ApiExtFuncionarioService apiFuncionarioService,
            ApiDepartamentoService apiDepartamentoService,
            AprobacionRepository aprobacionRepository) {
        this.apiExtFuncionarioService = apiFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
        this.aprobacionRepository = aprobacionRepository;
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
        dto.setUrlPdf(getUrlPdf(solicitud));

        // Obtener nombre del funcionario
        FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
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

    private String getUrlPdf(Solicitud solicitud) {

        Optional<Aprobacion> optAprobacion = aprobacionRepository.findBySolicitud(solicitud);

        if (optAprobacion.isPresent()) {
            return optAprobacion.get().getUrlPdf();
        } else {
            return null;
        }

    }
}
