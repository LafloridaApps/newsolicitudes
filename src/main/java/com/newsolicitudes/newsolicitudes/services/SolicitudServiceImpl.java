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
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SolicitudService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;
import com.newsolicitudes.newsolicitudes.services.mapper.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final FuncionarioService funcionarioService;

    private final SolicitudRepository solicitudRepository;

    private final DerivacionService derivacionService;

    private final SubroganciaService subroganciaService;

    private final DepartamentoService departamentoService;

    private final SolicitudMapper solicitudMapper;

    public SolicitudServiceImpl(DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            SolicitudMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {

        FuncionarioResponse funcionario = funcionarioService.getFuncionarioByRut(request.getRut());

        DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(funcionario.getCodDepto());

        DepartamentoResponse departamentoDestino = departamentoService.getDepartamentoDestino(request.getRut(),
                departamentoActual);

        NivelDepartamento nivelDepartamento = DepartamentoUtils.getNivelDepartamento(departamentoDestino);

        TipoDerivacion tipoDerivacion = DepartamentoUtils.tipoPorNivel(nivelDepartamento);

        Solicitud solicitud = solicitudMapper.mapToSolicitud(request, funcionario.getRut(), funcionario.getCodDepto());

        solicitud = solicitudRepository.save(solicitud);

        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamentoDestino.getId(),
                EstadoDerivacion.PENDIENTE);

        if (request.getSubrogancia() != null) {
            createSubroganciaSol(request.getSubrogancia(), request.getFechaInicio(), request.getFechaFin(),
                    request.getDepto());

        }

        return new SolicitudResponse(solicitud.getId(), departamentoDestino.getNombre());

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