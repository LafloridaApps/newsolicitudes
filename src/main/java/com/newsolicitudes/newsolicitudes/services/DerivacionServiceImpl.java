package com.newsolicitudes.newsolicitudes.services;

import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.mapper.SolicitudMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public DerivacionServiceImpl(
            DerivacionRepository derivacionRepository,
            EntradaDerivacionRepository entradaDerivacionRepository,
            SolicitudMapper solicitudDtoMapper) {
        this.derivacionRepository = derivacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
        this.solicitudDtoMapper = solicitudDtoMapper;
    }

    @Override
    @Transactional(rollbackFor = DerivacionExceptions.class)
    public void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo,
            Long idDpeto, EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions {

        Derivacion derivacionInicial = new Derivacion();
        derivacionInicial.setSolicitud(solicitud);
        derivacionInicial.setIdDepto(idDpeto);
        derivacionInicial.setFechaDerivacion(LocalDate.now());
        derivacionInicial.setEstadoDerivacion(estadoDerivacion);
        derivacionInicial.setTipo(tipo);

        derivacionRepository.save(derivacionInicial);
    }

    @Override
    public PageSolicitudesResponse getDerivacionesByDeptoId(Long idDepto, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);

        Page<Derivacion> derivacionesPage = derivacionRepository.findByIdDepto(idDepto, pageable);

        List<SolicitudDto> sortedSolicitudes = derivacionesPage.getContent().stream()
                .map(derivacion -> {
                    Solicitud solicitud = derivacion.getSolicitud(); // ya está disponible

                    SolicitudDto dto = solicitudDtoMapper(solicitud);

                    DerivacionDto derivacionDto = getDerivacionDto(derivacion);
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

   
}