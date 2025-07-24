package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SolicitudService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final FuncionarioRepository funcionarioRepository;

    private final ApiFuncionarioService apiFuncionarioService;

    private final FuncionarioService funcionarioService;

    private final SolicitudRepository solicitudRepository;

    private final DerivacionService derivacionService;

    private final DepartamentoRepository departamentoRepository;

    private final SubroganciaService subroganciaService;

    private final DepartamentoService departamentoService;

    public SolicitudServiceImpl(ApiFuncionarioService apiFuncionarioService, FuncionarioService funcionarioService,
            DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService, DepartamentoService departamentoService,
            DepartamentoRepository departamentoRepository, FuncionarioRepository funcionarioRepository) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.funcionarioService = funcionarioService;
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
    }

    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {

        ApiFuncionarioResponse funcionario = apiFuncionarioService.obtenerDetalleColaborador(request.getRut());

        Funcionario solicitante = funcionarioService.getOrCreateFuncionario(funcionario);

        Departamento departamento = getDepartamentoById(request.getDepto());

        departamento = departamentoService.getDepartamentoDestino(departamento, funcionario.getRut());

        Solicitud solicitud = mapToSolicitud(request, solicitante, departamento);

        TipoDerivacion tipoDerivacion = getTipoDerivacion(departamento, solicitante, solicitud);

        solicitud = solicitudRepository.save(solicitud);

        Derivacion derivacion = derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamento,
                EstadoDerivacion.PENDIENTE);

        if (request.getSubrogancia() != null) {
            createSubroganciaSol(request.getSubrogancia(), request.getFechaInicio(), request.getFechaFin(),
                    request.getDepto());

        }

        return new SolicitudResponse(solicitud.getId(), derivacion.getNombreDepartamento());

    }

    private Solicitud mapToSolicitud(SolicitudRequest request, Funcionario solicitante, Departamento departamento) {
        Solicitud solicitud = new Solicitud();
        solicitud.setCantidadDias(request.getDiasUsar());
        solicitud.setFechaSolicitud(request.getFechaSolicitud());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaTermino(request.getFechaFin());
        solicitud.setSolicitante(solicitante);
        solicitud.setDepartamento(departamento);
        solicitud.setTipoSolicitud(Solicitud.TipoSolicitud.valueOf(request.getTipoSolicitud()));
        solicitud.setEstado(Solicitud.EstadoSolicitud.PENDIENTE);
        solicitud.setJornadaInicio(
                request.getJornadaInicio() != null ? Solicitud.Jornada.valueOf(request.getJornadaInicio()) : null);
        solicitud.setJornadaTermino(
                request.getJornadaTermino() != null ? Solicitud.Jornada.valueOf(request.getJornadaTermino()) : null);

        return solicitud;
    }

    private TipoDerivacion getTipoDerivacion(Departamento departamento, Funcionario solicitante, Solicitud solicitud) {
        if (departamento == null) {
            return TipoDerivacion.VISACION;
        }

        if (esJefeDelDepartamento(departamento, solicitante)) {
            return TipoDerivacion.FIRMA;
        }

        if (subroganciaService.estaSubrogandoNivelSuperior(departamento, solicitud)) {
            return TipoDerivacion.FIRMA;
        }

        return tipoPorNivel(departamento.getNivel());
    }

    private boolean esJefeDelDepartamento(Departamento departamento, Funcionario funcionario) {
        return departamento != null && departamento.getJefe() != null && departamento.getJefe().equals(funcionario);
    }

    private TipoDerivacion tipoPorNivel(Departamento.NivelDepartamento nivel) {
        return switch (nivel) {
            case DEPARTAMENTO, SECCION, OFICINA -> TipoDerivacion.VISACION;
            case ALCALDIA, DIRECCION, SUBDIRECCION, ADMINISTRACION -> TipoDerivacion.FIRMA;
            default -> TipoDerivacion.VISACION;
        };
    }

    private Departamento getDepartamentoById(Long id) {
        return RepositoryUtils.findOrThrow(departamentoRepository.findById(id),
                String.format("Departamento %d no encontrado", id));
    }

    private Funcionario getFuncionarioByRut(Integer rut) {
        return RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                String.format("Funcionario con rut %d no encontrado", rut));
    }

    private void createSubroganciaSol(SubroganciaRequest subrogancia, LocalDate fechaInicio, LocalDate fechaFin,
            Long idDepto) {

        subroganciaService.createSubrogancia(subrogancia, fechaInicio, fechaFin, idDepto);

    }

    @Override
    public boolean existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo) {

        Funcionario funcionario = getFuncionarioByRut(rut);

        return solicitudRepository.findBySolicitanteAndFechaInicioAndTipoSolicitud(funcionario, fechaInicio,
                Solicitud.TipoSolicitud.valueOf(tipo)).isPresent();

    }
}