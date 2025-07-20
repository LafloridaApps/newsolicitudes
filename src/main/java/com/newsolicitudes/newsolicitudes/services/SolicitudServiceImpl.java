package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SolicitudService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final FuncionarioRepository funcionarioRepository;

    private final ApiFuncionarioService apiFuncionarioService;

    private final FuncionarioService funcionarioService;

    private final SolicitudRepository solicitudRepository;

    private final DerivacionService derivacionService;

    private final SubroganciaRepository subroganciaRepository;

    private final DepartamentoRepository departamentoRepository;

    

    public SolicitudServiceImpl(ApiFuncionarioService apiFuncionarioService, FuncionarioService funcionarioService,
            DerivacionService derivacionService,
            SolicitudRepository solicitudRepository, SubroganciaRepository subroganciaRepository,
            DepartamentoRepository departamentoRepository, FuncionarioRepository funcionarioRepository) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.funcionarioService = funcionarioService;
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaRepository = subroganciaRepository;
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {

        ApiFuncionarioResponse funcionario = apiFuncionarioService.obtenerDetalleColaborador(request.getRut());

        Funcionario solicitante = funcionarioService.getOrCreateFuncionario(funcionario);

        Departamento departamento = getDepartamentoById(request.getDepto());

        Solicitud solicitud = new Solicitud();
        solicitud.setSolicitante(solicitante);
        solicitud.setDepartamento(departamento);
        solicitud.setFechaSolicitud(request.getFechaSolicitud());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaTermino(request.getFechaFin());
        solicitud.setTipoSolicitud(Solicitud.TipoSolicitud.valueOf(request.getTipoSolicitud()));
        solicitud.setEstado(Solicitud.EstadoSolicitud.PENDIENTE);
        solicitud.setJornadaInicio(
                request.getJornadaInicio() != null ? Solicitud.Jornada.valueOf(request.getJornadaInicio()) : null);
        solicitud.setJornadaTermino(
                request.getJornadaTermino() != null ? Solicitud.Jornada.valueOf(request.getJornadaTermino()) : null);

        TipoDerivacion tipoDerivacion = getTipoDerivacion(departamento, solicitante, solicitud);

        solicitud = solicitudRepository.save(solicitud);

        Derivacion derivacion = derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamento,
                EstadoDerivacion.PENDIENTE);

        return new SolicitudResponse(solicitud.getId(), derivacion.getNombreDepartamento());

    }

    public TipoDerivacion getTipoDerivacion(Departamento departamento, Funcionario solicitante, Solicitud solicitud) {
        if (departamento == null) {
            return TipoDerivacion.VISACION;
        }

        if (esJefeDelDepartamento(departamento, solicitante)) {
            return TipoDerivacion.FIRMA;
        }

        if (estaSubrogandoNivelSuperior(departamento, solicitud)) {
            return TipoDerivacion.FIRMA;
        }

        return tipoPorNivel(departamento.getNivel());
    }

    private boolean esJefeDelDepartamento(Departamento departamento, Funcionario funcionario) {
        return departamento != null && departamento.getJefe() != null && departamento.getJefe().equals(funcionario);
    }

    private boolean estaSubrogandoNivelSuperior(Departamento departamento, Solicitud solicitud) {
        if (departamento == null || departamento.getJefe() == null) {
            return false;
        }

        Funcionario jefe = departamento.getJefe();
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubrogante(jefe);

        for (Subrogancia sub : subrogancias) {
            if (subroganciaVigente(sub, solicitud)
                    && subrogaNivelConFirma(sub.getDepartamento(), departamento)) {
                return true;
            }
        }

        return false;
    }

    private boolean subroganciaVigente(Subrogancia sub, Solicitud solicitud) {
        return (sub.getFechaInicio().isEqual(solicitud.getFechaInicio())
                || sub.getFechaInicio().isBefore(solicitud.getFechaInicio()))
                && (sub.getFechaFin().isEqual(solicitud.getFechaTermino())
                        || sub.getFechaFin().isAfter(solicitud.getFechaTermino()));
    }

    private boolean subrogaNivelConFirma(Departamento subrogado, Departamento actual) {
        if (subrogado == null || actual == null) {
            return false;
        }

        Departamento.NivelDepartamento nivelSubrogado = subrogado.getNivel();
        Departamento.NivelDepartamento nivelActual = actual.getNivel();

        boolean esNivelSuperior = nivelSubrogado.ordinal() < nivelActual.ordinal();
        boolean puedeFirmar = switch (nivelSubrogado) {
            case DIRECCION, SUBDIRECCION, ADMINISTRACION, ALCALDIA -> true;
            default -> false;
        };

        return esNivelSuperior && puedeFirmar;
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

    @Override
    public boolean existeSolicitudByFechaAndTipo(Integer rut,LocalDate fechaInicio, String tipo) {

        Funcionario funcionario  = getFuncionarioByRut(rut);
       
        return solicitudRepository.findBySolicitanteAndFechaInicioAndTipoSolicitud(funcionario,fechaInicio,
                Solicitud.TipoSolicitud.valueOf(tipo)).isPresent();
        
    }
}