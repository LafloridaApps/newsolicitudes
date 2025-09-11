package com.newsolicitudes.newsolicitudes.services.mapper;

import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.PdfDto;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.TipoSolicitud;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

@Component
public class PdfDtoMapper {

    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;
    private final AprobacionRepository aprobacionRepository;

    public PdfDtoMapper(ApiExtFuncionarioService apiExtFuncionarioService,
            ApiDepartamentoService apiDepartamentoService,
            AprobacionRepository aprobacionRepository) {
        this.apiExtFuncionarioService = apiExtFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
        this.aprobacionRepository = aprobacionRepository;
    }

    public PdfDto toPdfDto(Solicitud solicitud) {
        FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());
        FuncionarioResponseApi jefe = apiExtFuncionarioService.obtenerDetalleColaborador(departamento.getRutJefe());
        FuncionarioResponseApi director = apiExtFuncionarioService.obtenerDetalleColaborador(getRutFirma(solicitud));

        return PdfDto.builder()
                .idSol(solicitud.getId())
                .jornada(getJornada(solicitud))
                .nroIniDia(String.valueOf(solicitud.getFechaInicio().getDayOfMonth()))
                .mesIni(solicitud.getFechaInicio().getMonth().getDisplayName(TextStyle.FULL, Locale.of("es", "ES")))
                .nroFinDia(String.valueOf(solicitud.getFechaTermino().getDayOfMonth()))
                .mesFin(solicitud.getFechaTermino().getMonth().getDisplayName(TextStyle.FULL, Locale.of("es", "ES")))
                .diasTomados(String.valueOf(solicitud.getCantidadDias()))
                .rut(String.valueOf(funcionario.getRut()))
                .vrut(funcionario.getVrut())
                .paterno(funcionario.getApellidoPaterno())
                .materno(funcionario.getApellidoMaterno())
                .nombres(funcionario.getNombre())
                .depto(departamento.getNombre())
                .escalafon(funcionario.getTipoContrato()) 
                .grado(funcionario.getGrado().toString())
                .telefono("0") 
                .rutJefe(String.valueOf(jefe.getRut()))
                .nombreJefe(jefe.getNombre() + " " + jefe.getApellidoPaterno())
                .rutDirector(String.valueOf(director.getRut()))
                .nombreDirector(director.getNombre() + " " + director.getApellidoPaterno())
                .tipoSolicitud(getTipoSolicitud(solicitud.getTipoSolicitud()))
                .build();
    }

    private Integer getRutFirma(Solicitud solicitud) {

        Optional<Aprobacion> aprobacion = aprobacionRepository.findBySolicitud(solicitud);

        if (aprobacion.isPresent()) {
            return aprobacion.get().getRut();
        } else {
            return null;
        }

    }

    private Long getTipoSolicitud(TipoSolicitud tipoSolicitud) {
        return switch (tipoSolicitud) {
            case FERIADO -> 1L;
            case ADMINISTRATIVO -> 2L;
        };
    }

    private String getJornada(Solicitud solicitud) {
        if (solicitud.getJornadaInicio() == null) {
            return "Completa"; // Assume "Completa" when null
        }
        return switch (solicitud.getJornadaInicio()) {
            case PM -> "PM";
            case AM -> "AM";
            case COMPLETA -> "Completa";
            default -> ""; // This default should ideally not be reached if all enum values are covered
        };
    }
}
