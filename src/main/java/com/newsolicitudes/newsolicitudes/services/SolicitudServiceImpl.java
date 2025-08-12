package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.List;

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
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Visacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.VisacionRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SolicitudService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;
import com.newsolicitudes.newsolicitudes.services.mapper.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final FuncionarioService funcionarioService;

    private final SolicitudRepository solicitudRepository;

    private final DerivacionService derivacionService;

    private final SubroganciaService subroganciaService;

    private final DepartamentoService departamentoService;

    private final SolicitudMapper solicitudMapper;

    private final VisacionRepository visacionRepository;

    private final AprobacionRepository aprobacionRepository;

    public SolicitudServiceImpl(DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            SolicitudMapper solicitudMapper, VisacionRepository visacionRepository,
            AprobacionRepository aprobacionRepository) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.solicitudMapper = solicitudMapper;
        this.visacionRepository = visacionRepository;
        this.aprobacionRepository = aprobacionRepository;
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

    @Override
    public PageMiSolicitudResponse getSolicitudesByRut(Integer rut) {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Solicitud> solicitudes = solicitudRepository.findByRut(rut, pageable);

        List<Solicitud> solicitudesList = solicitudes.getContent();

        List<MiSolicitudDto> miSolicitudes = solicitudesList.stream().map(this::mapToMiSolicitudDto).toList();

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

        List<Trazabilidad> trazabilidad = solicitud.getDerivaciones().stream().map(this::mapToTrazabilidad).toList();

        miSolicitudDto.setTrazabilidad(trazabilidad);

        return miSolicitudDto;

    }

    private Trazabilidad mapToTrazabilidad(Derivacion derivacion) {

        Trazabilidad trazabilidad = new Trazabilidad();
        trazabilidad.setId(derivacion.getId());
        trazabilidad.setFecha(derivacion.getFechaDerivacion().toString());
        trazabilidad.setAccion(derivacion.getTipo().name());
        if(derivacion.getTipo().name().equals("VISACION")){
            
        }
        trazabilidad.setUsuario("Falta agrega campo");
        trazabilidad.setDepartamento(derivacion.getIdDepto().toString());

        return trazabilidad;

    }

    private boolean hasVisacion(Derivacion derivacion) {

        return visacionRepository.findBySolicitud(derivacion.getSolicitud()).isPresent();

    }

    private boolean hasAprobacion(Derivacion derivacion) {

        return aprobacionRepository.findBySolicitud(derivacion.getSolicitud()).isPresent();

    }

    private Visacion getVisacionById(Long id) {

        return RepositoryUtils.findOrThrow(visacionRepository.findById(id),
                String.format("No se encuentra la visacion %d", id));

    }

    private Aprobacion getAprobacionById(Long id) {

        return RepositoryUtils.findOrThrow(aprobacionRepository.findById(id),
                String.format("No se encuentra la aprobacion %d", id));

    }
}