package com.newsolicitudes.newsolicitudes.service.aprobacioneslist;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

@Service
public class AprobacionListServiceImpl implements AprobacionListService {

    private final AprobacionRepository aprobacionRepository;

    private final FuncionarioService funcionarioService;

    private final DepartamentoService departamentoService;

    public AprobacionListServiceImpl(AprobacionRepository aprobacionRepository, FuncionarioService funcionarioService,
            DepartamentoService departamentoService) {
        this.aprobacionRepository = aprobacionRepository;
        this.funcionarioService = funcionarioService;
        this.departamentoService = departamentoService;
    }

    @Override
    public List<AprobacionList> getAprobacionList(LocalDate fechaInicio, LocalDate fechaTermino) {

        List<Aprobacion> aprobaciones = aprobacionRepository.findByFechaAprobacionBetween(fechaInicio, fechaTermino);

        return aprobaciones.stream().map(aprobacion -> AprobacionList.builder()
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
                .url("")
                .build()).toList();

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

    private String obtenerJornada(Solicitud solicitud) {
        Solicitud.Jornada jornadaInicio = solicitud.getJornadaInicio();
        Solicitud.Jornada jornadaTermino = solicitud.getJornadaTermino();

        if (jornadaInicio == null || jornadaTermino == null) {
            return "SIN JORNADA";
        }

        // Si ambas son iguales
        if (jornadaInicio == jornadaTermino) {
            return jornadaInicio.name(); // devuelve "COMPLETA", "AM" o "PM"
        }

        // Si combinan AM y PM → se interpreta como COMPLETA
        if ((jornadaInicio == Solicitud.Jornada.AM && jornadaTermino == Solicitud.Jornada.PM) ||
                (jornadaInicio == Solicitud.Jornada.PM && jornadaTermino == Solicitud.Jornada.AM)) {
            return Solicitud.Jornada.COMPLETA.name();
        }

        // Si llega aquí, se devuelve un rango de jornadas
        return jornadaInicio.name() + " - " + jornadaTermino.name();
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
