package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SolicitudService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final ApiFuncionarioService apiFuncionarioService;

    private final ApiDepartamentoService apiDepartamentoService;

    private final SolicitudRepository solicitudRepository;

    private final DerivacionService derivacionService;

    private final SubroganciaService subroganciaService;

    public SolicitudServiceImpl(ApiFuncionarioService apiFuncionarioService,
            DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService, ApiDepartamentoService apiDepartamentoService) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {

        FuncionarioResponse funcionario = apiFuncionarioService.obtenerDetalleColaborador(request.getRut());

        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(funcionario.getCodDepto());

        NivelDepartamento nivelDepartamento = NivelDepartamento.valueOf(departamento.getNivelDepartamento());

        TipoDerivacion tipoDerivacion = tipoPorNivel(nivelDepartamento);

        Solicitud solicitud = mapToSolicitud(request, funcionario.getRut(), funcionario.getCodDepto());

        solicitud = solicitudRepository.save(solicitud);

        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, funcionario.getCodDepto(),
                EstadoDerivacion.PENDIENTE);

        if (request.getSubrogancia() != null) {
            createSubroganciaSol(request.getSubrogancia(), request.getFechaInicio(), request.getFechaFin(),
                    request.getDepto());

        }

        return new SolicitudResponse(solicitud.getId(), departamento.getNombre());

    }

    private Solicitud mapToSolicitud(SolicitudRequest request, Integer solicitante, Long idDepto) {
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

    private TipoDerivacion tipoPorNivel(NivelDepartamento nivel) {
        return switch (nivel) {
            case DEPARTAMENTO, SECCION, OFICINA -> TipoDerivacion.VISACION;
            case ALCALDIA, DIRECCION, SUBDIRECCION, ADMINISTRACION -> TipoDerivacion.FIRMA;
            default -> TipoDerivacion.VISACION;
        };
    }

    private void createSubroganciaSol(SubroganciaRequest subrogancia, LocalDate fechaInicio, LocalDate fechaFin,
            Long idDepto) {

        subroganciaService.createSubrogancia(subrogancia, fechaInicio, fechaFin, idDepto);

    }

    @Override
    public boolean existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo) {

        return solicitudRepository.findByRutAndFechaInicioAndTipoSolicitud(rut, fechaInicio,
                Solicitud.TipoSolicitud.valueOf(tipo)).isPresent();

    }
}