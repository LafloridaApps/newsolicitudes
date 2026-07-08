package com.newsolicitudes.newsolicitudes.services.busqueda;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.SolicitudAnulacion;
import com.newsolicitudes.newsolicitudes.mappers.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudAnulacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BusquedaSolicitudServiceImpl implements BusquedaSolicitudService {

    private static final Logger logger = LoggerFactory.getLogger(BusquedaSolicitudServiceImpl.class);

    private final SolicitudRepository solicitudRepository;
    private final DerivacionRepository derivacionRepository;
    private final EntradaDerivacionRepository entradaDerivacionRepository;
    private final SolicitudAnulacionRepository solicitudAnulacionRepository;
    private final AprobacionRepository aprobacionRepository;
    private final SolicitudMapper solicitudMapper;
    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;
    private final ApiExtFuncionarioService apiExtFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;

    public BusquedaSolicitudServiceImpl(SolicitudRepository solicitudRepository,
            DerivacionRepository derivacionRepository,
            EntradaDerivacionRepository entradaDerivacionRepository,
            SolicitudAnulacionRepository solicitudAnulacionRepository,
            AprobacionRepository aprobacionRepository,
            SolicitudMapper solicitudMapper,
            FuncionarioService funcionarioService,
            DepartamentoService departamentoService,
            ApiExtFuncionarioService apiExtFuncionarioService,
            ApiDepartamentoService apiDepartamentoService) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionRepository = derivacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
        this.solicitudAnulacionRepository = solicitudAnulacionRepository;
        this.aprobacionRepository = aprobacionRepository;
        this.solicitudMapper = solicitudMapper;
        this.funcionarioService = funcionarioService;
        this.departamentoService = departamentoService;
        this.apiExtFuncionarioService = apiExtFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    public PageSolicitudesResponse buscarSolicitudes(Long codDepto, String nombreSolicitante, String rutSolicitante,
            String fechaInicio, String fechaTermino, int pageNumber) {

        List<Long> deptoIds = obtenerDeptoIds(codDepto);
        List<Integer> ruts = obtenerRuts(nombreSolicitante, rutSolicitante);

        LocalDate fechaInicioParsed = fechaInicio != null && !fechaInicio.isBlank()
                ? LocalDate.parse(fechaInicio)
                : null;
        LocalDate fechaTerminoParsed = fechaTermino != null && !fechaTermino.isBlank()
                ? LocalDate.parse(fechaTermino)
                : null;

        Pageable pageable = PageRequest.of(pageNumber, 10);
        Page<Solicitud> solicitudesPage = solicitudRepository.buscarPorCriterios(
                deptoIds, fechaInicioParsed, fechaTerminoParsed,
                ruts != null && ruts.isEmpty() ? null : ruts,
                pageable);

        List<SolicitudDto> solicitudesDto = solicitudesPage.getContent().stream()
                .map(this::mapearSolicitudADto)
                .toList();

        return construirRespuesta(solicitudesPage, solicitudesDto);
    }

    private List<Long> obtenerDeptoIds(Long codDepto) {
        if (codDepto == null) {
            return new ArrayList<>();
        }
        try {
            List<DepartamentoResponse> familia = apiDepartamentoService.obtenerFamiliaDepto(codDepto);
            if (familia != null && !familia.isEmpty()) {
                return familia.stream()
                        .map(DepartamentoResponse::getId)
                        .toList();
            }
        } catch (Exception e) {
            logger.error("Error al obtener familia de departamentos para codDepto {}: {}", codDepto, e.getMessage(), e);
        }
        return Collections.singletonList(codDepto);
    }

    private List<Integer> obtenerRuts(String nombreSolicitante, String rutSolicitante) {
        List<Integer> ruts = new ArrayList<>();

        if (rutSolicitante != null && !rutSolicitante.isBlank()) {
            Integer rutNumerico = Integer.parseInt(rutSolicitante.replaceAll("-.*", ""));
            ruts.add(rutNumerico);
        }

        if (nombreSolicitante != null && !nombreSolicitante.isBlank()) {
            ruts = buscarRutsPorNombre(nombreSolicitante, ruts);
        }

        return ruts.isEmpty() ? null : ruts;
    }

    private List<Integer> buscarRutsPorNombre(String nombreSolicitante, List<Integer> ruts) {
        try {
            SearchFuncionarioResponse response = apiExtFuncionarioService
                    .buscarFuncionarioByNombre(nombreSolicitante, 0);
            if (response != null && response.getFuncionarios() != null) {
                List<Integer> rutsPorNombre = response.getFuncionarios().stream()
                        .map(f -> f.getRut())
                        .toList();
                if (ruts.isEmpty()) {
                    return rutsPorNombre;
                }
                ruts.retainAll(rutsPorNombre);
            }
        } catch (Exception e) {
            logger.error("Error al buscar funcionarios por nombre: {}", e.getMessage(), e);
        }
        return ruts;
    }

    private SolicitudDto mapearSolicitudADto(Solicitud solicitud) {
        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(solicitud.getRut());
        DepartamentoResponse departamento = departamentoService.getDepartamentoById(solicitud.getIdDepto());
        String urlPdf = obtenerUrlPdf(solicitud);

        SolicitudDto dto = solicitudMapper.solicitudToSolicitudDto(solicitud, funcionario, departamento, urlPdf);

        List<Derivacion> derivaciones = derivacionRepository.findBySolicitudId(solicitud.getId());
        List<DerivacionDto> derivacionesDto = derivaciones.stream()
                .map(this::mapearDerivacionADto)
                .toList();
        dto.setDerivaciones(derivacionesDto);

        boolean tieneAnulacionPendiente = solicitudAnulacionRepository
                .existsBySolicitudIdAndEstado(solicitud.getId(),
                        SolicitudAnulacion.EstadoSolicitudAnulacion.PENDIENTE);
        dto.setTieneAnulacionPendiente(tieneAnulacionPendiente);

        return dto;
    }

    private DerivacionDto mapearDerivacionADto(Derivacion derivacion) {
        DerivacionDto dto = new DerivacionDto();
        dto.setId(derivacion.getId());
        dto.setFechaDerivacion(derivacion.getFechaDerivacion().toString());
        dto.setEstadoDerivacion(derivacion.getEstadoDerivacion().name());
        dto.setTipoMovimiento(derivacion.getTipo().name());
        dto.setRecepcionada(entradaDerivacionRepository.findByDerivacionId(derivacion.getId()).isPresent());

        DepartamentoResponse depto = departamentoService.getDepartamentoById(derivacion.getIdDepto());
        dto.setNombreDepartamento(depto != null ? depto.getNombre() : null);

        return dto;
    }

    private String obtenerUrlPdf(Solicitud solicitud) {
        Optional<Aprobacion> optAprobacion = aprobacionRepository.findBySolicitud(solicitud);
        return optAprobacion.map(Aprobacion::getUrlPdf).orElse(null);
    }

    private PageSolicitudesResponse construirRespuesta(Page<Solicitud> page, List<SolicitudDto> content) {
        PageSolicitudesResponse response = new PageSolicitudesResponse();
        response.setSolicitudes(content);
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        return response;
    }
}
