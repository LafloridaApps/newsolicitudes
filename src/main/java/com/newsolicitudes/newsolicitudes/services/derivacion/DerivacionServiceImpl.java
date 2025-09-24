package com.newsolicitudes.newsolicitudes.services.derivacion;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.mapper.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class DerivacionServiceImpl implements DerivacionService {

    private final DerivacionRepository derivacionRepository;
    private final EntradaDerivacionRepository entradaDerivacionRepository;
    private final SolicitudMapper solicitudDtoMapper;
    private final SubroganciaRepository subroganciaRepository;
    private final DepartamentoService departamentoService;
    private final FuncionarioService funcionarioService;

    public DerivacionServiceImpl(
            DerivacionRepository derivacionRepository,
            EntradaDerivacionRepository entradaDerivacionRepository,
            SolicitudMapper solicitudDtoMapper,
            SubroganciaRepository subroganciaRepository, DepartamentoService departamentoService,
            FuncionarioService funcionarioService) {
        this.derivacionRepository = derivacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
        this.solicitudDtoMapper = solicitudDtoMapper;
        this.subroganciaRepository = subroganciaRepository;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
    }

    @Override
    @Transactional(rollbackFor = DerivacionExceptions.class)
    public void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo,
            Long idDepto, EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions {

        DepartamentoResponse depto = departamentoService.getDepartamentoById(idDepto);
        TipoDerivacion tipoFinal = determinaTipoDerivacionFinal(depto, solicitud.getFechaInicio());

        Derivacion derivacionInicial = new Derivacion();
        derivacionInicial.setSolicitud(solicitud);
        derivacionInicial.setIdDepto(idDepto);
        derivacionInicial.setFechaDerivacion(LocalDate.now());
        derivacionInicial.setEstadoDerivacion(estadoDerivacion);
        derivacionInicial.setTipo(tipoFinal);

        derivacionRepository.save(derivacionInicial);
    }

    private TipoDerivacion determinaTipoDerivacionFinal(DepartamentoResponse deptoDestino, LocalDate fechaChequeo) {
        NivelDepartamento nivelDeptoDestino = DepartamentoUtils.getNivelDepartamento(deptoDestino);
        TipoDerivacion tipoFinal = DepartamentoUtils.tipoPorNivel(nivelDeptoDestino);

        TipoDerivacion tipoPorSubroganciaJefe = getTipoSiJefeEsSubroganteDirector(deptoDestino.getRutJefe(), fechaChequeo);
        if (tipoPorSubroganciaJefe == TipoDerivacion.FIRMA) {
            return TipoDerivacion.FIRMA;
        }

        TipoDerivacion tipoPorJefeSubrogado = getTipoSiJefeEstaSiendoSubrogadoPorDirector(deptoDestino.getRutJefe(), fechaChequeo);
        if (tipoPorJefeSubrogado == TipoDerivacion.FIRMA) {
            return TipoDerivacion.FIRMA;
        }

        return tipoFinal;
    }

    private TipoDerivacion getTipoSiJefeEsSubroganteDirector(Integer rutJefe, LocalDate fechaChequeo) {
        List<Subrogancia> subrogancias = subroganciaRepository
                .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, fechaChequeo,
                        fechaChequeo);
        if (subrogancias.isEmpty()) {
            return null;
        }
        for (Subrogancia subrogancia : subrogancias) {
            DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
            NivelDepartamento nivel = DepartamentoUtils.getNivelDepartamento(deptoSubrogado);
            if (DepartamentoUtils.tipoPorNivel(nivel) == TipoDerivacion.FIRMA) {
                return TipoDerivacion.FIRMA;
            }
        }
        return null;
    }

    private TipoDerivacion getTipoSiJefeEstaSiendoSubrogadoPorDirector(Integer rutJefeOriginal, LocalDate fechaChequeo) {
        List<Subrogancia> subrogancias = subroganciaRepository
                .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeOriginal,
                        fechaChequeo, fechaChequeo);
        if (subrogancias.isEmpty()) {
            return null;
        }
        for (Subrogancia subrogancia : subrogancias) {
            Integer rutSubrogante = subrogancia.getSubrogante();
            FuncionarioResponseApi funcionarioSubrogante = funcionarioService.getFuncionarioByRut(rutSubrogante);
            DepartamentoResponse deptoSubrogante = departamentoService
                    .getDepartamentoById(funcionarioSubrogante.getCodDepto());
            NivelDepartamento nivelSubrogante = DepartamentoUtils.getNivelDepartamento(deptoSubrogante);
            if (DepartamentoUtils.tipoPorNivel(nivelSubrogante) == TipoDerivacion.FIRMA) {
                return TipoDerivacion.FIRMA;
            }
        }
        return null;
    }

    @Override
    public PageSolicitudesResponse getDerivacionesByDeptoId(Long idDepto, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10,Sort.by("solicitud.id").descending());

        List<Subrogancia> subrogancias = getSubroganciasByRutSubrogante(idDepto);
        List<Long> deptoIds = new java.util.ArrayList<>();
        deptoIds.add(idDepto);

        if (!subrogancias.isEmpty()) {
            deptoIds.addAll(subrogancias.stream().map(Subrogancia::getIdDepto).toList());
        }

        Page<Derivacion> derivacionesPage = derivacionRepository.findByIdDeptoIn(deptoIds, pageable);

        List<SolicitudDto> sortedSolicitudes = derivacionesPage.getContent().stream()
                .map(derivacion -> {
                    Solicitud solicitud = derivacion.getSolicitud();
                    SolicitudDto dto = solicitudDtoMapper(solicitud);
                    DerivacionDto derivacionDto = getDerivacionDto(derivacion);

                    boolean isSubrogado = subrogancias.stream()
                            .anyMatch(s -> s.getIdDepto().equals(derivacion.getIdDepto()));

                    if (isSubrogado) {
                        Subrogancia subrogancia = subrogancias.stream()
                                .filter(s -> s.getIdDepto().equals(derivacion.getIdDepto())).findFirst().get();
                        DepartamentoResponse deptoSubrogado = departamentoService
                                .getDepartamentoById(subrogancia.getIdDepto());
                        dto.setSubroganciaInfo(List.of(new com.newsolicitudes.newsolicitudes.dto.SubroganciaInfo(
                                deptoSubrogado.getId(), deptoSubrogado.getNombre(), subrogancia.getFechaInicio(),
                                subrogancia.getFechaFin())));

                    } else {
                        dto.setSubroganciaInfo(java.util.Collections.emptyList());
                    }

                    dto.setDerivaciones(List.of(derivacionDto));
                    return dto;
                })
                .sorted(Comparator.comparing(dto -> dto.getId(), Comparator.reverseOrder()))
                .toList();

        PageSolicitudesResponse solicitudesDtoResponse = new PageSolicitudesResponse();
        solicitudesDtoResponse.setSolicitudes(sortedSolicitudes);
        solicitudesDtoResponse.setTotalPages(derivacionesPage.getTotalPages());
        solicitudesDtoResponse.setTotalElements(derivacionesPage.getTotalElements());
        solicitudesDtoResponse.setCurrentPage(derivacionesPage.getNumber());

        return solicitudesDtoResponse;
    }

    private DerivacionDto getDerivacionDto(Derivacion derivacion) {
        DerivacionDto dto = new DerivacionDto();
        dto.setId(derivacion.getId());
        dto.setFechaDerivacion(derivacion.getFechaDerivacion().toString());
        dto.setEstadoDerivacion(derivacion.getEstadoDerivacion().name());
        dto.setRecepcionada(hasEntrada(derivacion));
        dto.setTipoMovimiento(derivacion.getTipo().name());
        return dto;
    }

    private SolicitudDto solicitudDtoMapper(Solicitud solicitud) {
        return solicitudDtoMapper.solicitudDtoMapper(solicitud);
    }

    private boolean hasEntrada(Derivacion derivacion) {
        return entradaDerivacionRepository.findByDerivacionId(derivacion.getId()).isPresent();
    }

    private List<Subrogancia> getSubroganciasByRutSubrogante(Long idDepto) {

        DepartamentoResponse depto = departamentoService.getDepartamentoById(idDepto);
        LocalDate hoy = FechaUtils.fechaActual();
        return subroganciaRepository
                .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(depto.getRutJefe(), hoy, hoy);

    }


}