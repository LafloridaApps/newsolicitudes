package com.newsolicitudes.newsolicitudes.services.decretos;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

@Component
public class AprobacionesDecretadasMapper {

    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;

    public AprobacionesDecretadasMapper(FuncionarioService funcionarioService,
            DepartamentoService departamentoService) {
        this.funcionarioService = funcionarioService;
        this.departamentoService = departamentoService;
    }

    AprobacionList maptoAprobacionList(Solicitud solicitud, FuncionarioResponseApi funcionario, Long nroDecreto) {

        return AprobacionList.builder()
                .nroDecreto(nroDecreto)
                .idSolicitud(solicitud.getId())
                .rut(getRut(funcionario.getRut()))
                .nombres(obtenerNombres(funcionario.getRut()))
                .apellidos(obtnerApellidos(funcionario.getRut()))
                .departamento(obtenerDepartamento(solicitud.getIdDepto()))
                .jornada(obtenerJornada(solicitud)) // Assuming jornada is relevant
                .desde(solicitud.getFechaInicio().toString())
                .hasta(solicitud.getFechaTermino().toString())
                .duracion(solicitud.getCantidadDias())
                .fechaSolicitud(solicitud.getFechaSolicitud().toString())
                .tipoSolicitud(solicitud.getTipoSolicitud().toString())
                .tipoContrato(getTipoContrato(funcionario.getRut()))
                .url("") // Adding missing url field
                .build();

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
                return "DIA";
            } else { // Es el mismo día
                if (solicitud.getJornadaInicio() != null) {
                    if (solicitud.getJornadaInicio().equals(Solicitud.Jornada.AM)
                            && solicitud.getJornadaTermino().equals(Solicitud.Jornada.AM)) {
                        return "AM";
                    } else if (solicitud.getJornadaInicio().equals(Solicitud.Jornada.PM)
                            && solicitud.getJornadaTermino().equals(Solicitud.Jornada.PM)) {
                        return "PM";
                    }
                }
                // Si es el mismo día y no es AM ni PM, o jornadaInicio es COMPLETA, se
                // considera completa por defecto
                return "DIA";
            }
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