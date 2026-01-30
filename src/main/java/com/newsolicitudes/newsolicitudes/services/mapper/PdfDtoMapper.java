package com.newsolicitudes.newsolicitudes.services.mapper;

import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.PdfDto;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.TipoSolicitud;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.firmante.FirmanteService;

@Component
public class PdfDtoMapper {

    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;
    private final FirmanteService firmanteService;

    public PdfDtoMapper(ApiExtFuncionarioService apiExtFuncionarioService,
            ApiDepartamentoService apiDepartamentoService,
            FirmanteService firmanteService) {
        this.apiExtFuncionarioService = apiExtFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
        this.firmanteService = firmanteService;
    }

    public PdfDto toPdfDto(Solicitud solicitud) {
        FuncionarioResponseApi funcionario = apiExtFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());

        FuncionarioResponseApi jefe = validaJefe(departamento);

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
                .anio(String.valueOf(solicitud.getFechaTermino().getYear()))
                .build();
    }

    private FuncionarioResponseApi validaJefe(DepartamentoResponse departamento) {

        FuncionarioResponseApi jefe = new FuncionarioResponseApi();
        if (departamento.getRutJefe() != null) {
            jefe = apiExtFuncionarioService.obtenerDetalleColaborador(departamento.getRutJefe());
        } else {
            jefe.setRut(0);
        }

        return jefe;

    }

    private Integer getRutFirma(Solicitud solicitud) {
        return firmanteService.getRutFirmante(solicitud);
    }

    private Long getTipoSolicitud(TipoSolicitud tipoSolicitud) {
        return switch (tipoSolicitud) {
            case FERIADO -> 1L;
            case ADMINISTRATIVO -> 2L;
        };
    }

    private String getJornada(Solicitud solicitud) {
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
}
