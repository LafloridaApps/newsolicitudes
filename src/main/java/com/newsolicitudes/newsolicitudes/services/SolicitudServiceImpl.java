package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.MiSolicitudDto;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.PageMiSolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.dto.Trazabilidad;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.enums.EstadoTrazabilidad;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
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
    private final AprobacionRepository aprobacionRepository;

    public SolicitudServiceImpl(DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            SolicitudMapper solicitudMapper,
            AprobacionRepository aprobacionRepository) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.solicitudMapper = solicitudMapper;
        this.aprobacionRepository = aprobacionRepository;
    }

    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {
        FuncionarioResponse funcionario = funcionarioService.getFuncionarioByRut(request.getRut());
        DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(funcionario.getCodDepto());
        DepartamentoResponse departamentoDestino = departamentoService.getDepartamentoDestino(request.getRut(), departamentoActual);
        NivelDepartamento nivelDepartamento = DepartamentoUtils.getNivelDepartamento(departamentoDestino);
        TipoDerivacion tipoDerivacion = DepartamentoUtils.tipoPorNivel(nivelDepartamento);
        Solicitud solicitud = solicitudMapper.mapToSolicitud(request, funcionario.getRut(), funcionario.getCodDepto());
        solicitud = solicitudRepository.save(solicitud);
        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamentoDestino.getId(), EstadoDerivacion.PENDIENTE);
        if (request.getSubrogancia() != null) {
            createSubroganciaSol(request.getSubrogancia(), request.getFechaInicio(), request.getFechaFin(), request.getDepto());
        }
        return new SolicitudResponse(solicitud.getId(), departamentoDestino.getNombre());
    }

    private void createSubroganciaSol(SubroganciaRequest subrogancia, LocalDate fechaInicio, LocalDate fechaFin, Long idDepto) {
        subroganciaService.createSubrogancia(subrogancia, fechaInicio, fechaFin, idDepto);
    }

    @Override
    public boolean existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo) {
        return solicitudRepository.findByRutAndFechaInicioAndTipoSolicitud(rut, fechaInicio, Solicitud.TipoSolicitud.valueOf(tipo)).isPresent();
    }

    @Override
    public PageMiSolicitudResponse getSolicitudesByRut(Integer rut) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Solicitud> solicitudes = solicitudRepository.findByRut(rut, pageable);
        List<MiSolicitudDto> miSolicitudes = solicitudes.getContent().stream()
                .map(this::mapToMiSolicitudDto)
                .collect(Collectors.toList());

        PageMiSolicitudResponse pageMiSolicitudResponse = new PageMiSolicitudResponse();
        pageMiSolicitudResponse.setSolicitudes(miSolicitudes);
        pageMiSolicitudResponse.setTotalPages(solicitudes.getTotalPages());
        pageMiSolicitudResponse.setTotalElements(solicitudes.getTotalElements());
        pageMiSolicitudResponse.setCurrentPage(solicitudes.getNumber());
        pageMiSolicitudResponse.setPageSize(solicitudes.getSize());

        return pageMiSolicitudResponse;
    }

    private MiSolicitudDto mapToMiSolicitudDto(Solicitud solicitud) {
        MiSolicitudDto miSolicitudDto = new MiSolicitudDto();
        miSolicitudDto.setId(solicitud.getId());
        miSolicitudDto.setFechaSolicitud(solicitud.getFechaSolicitud().toString());
        miSolicitudDto.setFechaInicio(solicitud.getFechaInicio().toString());
        miSolicitudDto.setFechaFin(solicitud.getFechaTermino().toString());
        miSolicitudDto.setTipoSolicitud(solicitud.getTipoSolicitud().name());
        miSolicitudDto.setEstadoSolicitud(solicitud.getEstado().name());
        miSolicitudDto.setCantidadDias(solicitud.getCantidadDias());
        miSolicitudDto.setTrazabilidad(solicitud.getDerivaciones().stream()
                .map(this::mapToTrazabilidad)
                .collect(Collectors.toList()));
        return miSolicitudDto;
    }

    private Trazabilidad mapToTrazabilidad(Derivacion derivacion) {
        Trazabilidad trazabilidad = new Trazabilidad();
        trazabilidad.setId(derivacion.getId());
        trazabilidad.setFecha(derivacion.getFechaDerivacion().toString());
        
        DepartamentoResponse depto = departamentoService.getDepartamentoById(derivacion.getIdDepto());
        trazabilidad.setDepartamento(depto.getNombre());

        if (derivacion.getTipo() == TipoDerivacion.VISACION) {
            trazabilidad.setAccion("Visación");
            EntradaDerivacion entrada = derivacion.getEntrada();
            if (entrada != null) {
                FuncionarioResponse fr = funcionarioService.getFuncionarioByRut(entrada.getRut());
                trazabilidad.setUsuario(fr.getNombre() + " " + fr.getApellidoPaterno());
                if (derivacion.getEstadoDerivacion() == EstadoDerivacion.DERIVADA || derivacion.getEstadoDerivacion() == EstadoDerivacion.FINALIZADA) {
                    trazabilidad.setEstado(EstadoTrazabilidad.REALIZADO);
                } else {
                    trazabilidad.setEstado(EstadoTrazabilidad.RECIBIDO);
                }
            } else {
                trazabilidad.setUsuario("Pendiente");
                trazabilidad.setEstado(EstadoTrazabilidad.PENDIENTE);
            }
        } else if (derivacion.getTipo() == TipoDerivacion.FIRMA) {
            trazabilidad.setAccion("Aprobación");
            Optional<Aprobacion> aprobacionOpt = aprobacionRepository.findBySolicitud(derivacion.getSolicitud());
            if (aprobacionOpt.isPresent()) {
                Aprobacion aprobacion = aprobacionOpt.get();
                FuncionarioResponse fr = funcionarioService.getFuncionarioByRut(aprobacion.getRut());
                trazabilidad.setUsuario(fr.getNombre() + " " + fr.getApellidoPaterno());
                trazabilidad.setEstado(EstadoTrazabilidad.REALIZADO);
            } else {
                EntradaDerivacion entrada = derivacion.getEntrada();
                if (entrada != null) {
                    FuncionarioResponse fr = funcionarioService.getFuncionarioByRut(entrada.getRut());
                    trazabilidad.setUsuario(fr.getNombre() + " " + fr.getApellidoPaterno());
                    trazabilidad.setEstado(EstadoTrazabilidad.RECIBIDO);
                } else {
                    trazabilidad.setUsuario("Pendiente");
                    trazabilidad.setEstado(EstadoTrazabilidad.PENDIENTE);
                }
            }
        }

        return trazabilidad;
    }
}