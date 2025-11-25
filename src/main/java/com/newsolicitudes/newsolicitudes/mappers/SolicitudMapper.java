package com.newsolicitudes.newsolicitudes.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.ExisteSolicitudResponseDto;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.MiSolicitudDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDetalleDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.Trazabilidad;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

@Component
public class SolicitudMapper {

    public SolicitudDto solicitudToSolicitudDto(Solicitud solicitud, FuncionarioResponseApi funcionario,
            DepartamentoResponse departamento, String urlPdf) {
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
        dto.setUrlPdf(urlPdf);

        if (funcionario != null) {
            dto.setNombreFuncionario(funcionario.getNombre() + " " + funcionario.getApellidoPaterno() + " "
                    + funcionario.getApellidoMaterno());
            dto.setRutSolicitante(funcionario.getRut() + "-" + funcionario.getVrut());
        }

        if (departamento != null) {
            dto.setNombreDepartamento(departamento.getNombre());
        }

        return dto;
    }

    public Solicitud solicitudRequestToSolicitud(SolicitudRequest request, Integer solicitante, Long idDepto,
            double cantidadDias) {
        Solicitud solicitud = new Solicitud();
        solicitud.setCantidadDias(cantidadDias);
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

    public SolicitudDetalleDto solicitudToSolicitudDetalleDto(Solicitud solicitud, String nombreFuncionario,
            String nombreDepartamento, String urlPdf, List<DerivacionDto> derivaciones) {
        SolicitudDetalleDto detalleDto = new SolicitudDetalleDto();
        detalleDto.setIdSolicitud(solicitud.getId());
        detalleDto.setRutFuncionario(solicitud.getRut());
        detalleDto.setNombreFuncionario(nombreFuncionario);
        detalleDto.setTipoSolicitud(solicitud.getTipoSolicitud().name());
        detalleDto.setFechaCreacion(solicitud.getFechaSolicitud().toString());
        detalleDto.setNombreDepartamento(nombreDepartamento);
        detalleDto.setFechaInicio(solicitud.getFechaInicio().toString());
        detalleDto.setFechaTermino(solicitud.getFechaTermino().toString());
        detalleDto.setEstadoSolicitud(solicitud.getEstado().name());
        detalleDto.setUrlPdf(urlPdf);
        detalleDto.setDerivaciones(derivaciones);
        detalleDto.setCantidadDias(solicitud.getCantidadDias());
        return detalleDto;
    }

    public DerivacionDto derivacionToDerivacionDto(Derivacion derivacion, String nombreDepartamento) {
        DerivacionDto dto = new DerivacionDto();
        dto.setId(derivacion.getId());
        dto.setFechaDerivacion(derivacion.getFechaDerivacion().toString());
        dto.setNombreDepartamento(nombreDepartamento);
        dto.setTipoMovimiento(derivacion.getTipo().name());
        dto.setEstadoDerivacion(derivacion.getEstadoDerivacion().name());
        dto.setRecepcionada(derivacion.getEntrada() != null);
        return dto;
    }

    public MiSolicitudDto solicitudToMiSolicitudDto(Solicitud solicitud, String urlPdf,
            List<Trazabilidad> trazabilidad) {
        MiSolicitudDto miSolicitudDto = new MiSolicitudDto();
        miSolicitudDto.setId(solicitud.getId());
        miSolicitudDto.setFechaSolicitud(solicitud.getFechaSolicitud().toString());
        miSolicitudDto.setFechaInicio(solicitud.getFechaInicio().toString());
        miSolicitudDto.setFechaFin(solicitud.getFechaTermino().toString());
        miSolicitudDto.setTipoSolicitud(solicitud.getTipoSolicitud().name());
        miSolicitudDto.setEstadoSolicitud(solicitud.getEstado().name());
        miSolicitudDto.setCantidadDias(solicitud.getCantidadDias());
        miSolicitudDto.setUrlPdf(urlPdf);
        miSolicitudDto.setTrazabilidad(trazabilidad);
        return miSolicitudDto;
    }

    public ExisteSolicitudResponseDto solicitudToExisteSolicitudResponseDto(Solicitud solicitud) {
        return new ExisteSolicitudResponseDto(
                true,
                solicitud.getEstado() != null ? solicitud.getEstado().name() : null,
                solicitud.getFechaInicio() != null ? solicitud.getFechaInicio().toString() : null,
                solicitud.getFechaTermino() != null ? solicitud.getFechaTermino().toString() : null,
                solicitud.getJornadaInicio() != null ? solicitud.getJornadaInicio().name() : null,
                solicitud.getJornadaTermino() != null ? solicitud.getJornadaTermino().name() : null);
    }
}
