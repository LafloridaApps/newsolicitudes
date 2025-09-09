package com.newsolicitudes.newsolicitudes.services.decretos;

import com.newsolicitudes.newsolicitudes.dto.DecretoConSolicitudesDTO;
import com.newsolicitudes.newsolicitudes.dto.SolicitudInfoDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DecretoDeleteRequest;
import com.newsolicitudes.newsolicitudes.dto.DecretoDto;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Decreto;
import com.newsolicitudes.newsolicitudes.entities.DecretoSolicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.DecretoRepository;
import com.newsolicitudes.newsolicitudes.repositories.DecretoSolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.exceptions.NotFounException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
public class DecretoServiceImpl implements DecretoService {

    private final SolicitudRepository solicitudRepository;
    private final DecretoRepository decretoRepository;
    private final DecretoSolicitudRepository decretoSolicitudRepository;
    private final FuncionarioService funcionarioService;
    private final AprobacionesDecretadasMapper mapper;
    private final DocumentoDecretoService documentoDecretoService;
    private final ApiExtFuncionarioService apiExtFuncionarioService;

    public DecretoServiceImpl(SolicitudRepository solicitudRepository, DecretoRepository decretoRepository,
            DecretoSolicitudRepository decretoSolicitudRepository, FuncionarioService funcionarioService,
            AprobacionesDecretadasMapper mapper, DocumentoDecretoService documentoDecretoService,
            ApiExtFuncionarioService apiExtFuncionarioService) {
        this.solicitudRepository = solicitudRepository;
        this.decretoRepository = decretoRepository;
        this.decretoSolicitudRepository = decretoSolicitudRepository;
        this.funcionarioService = funcionarioService;
        this.mapper = mapper;
        this.documentoDecretoService = documentoDecretoService;
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    @Transactional
    public List<AprobacionList> decretar(Set<Long> ids, Integer rut, String template) {

        List<Solicitud> solicitudes = solicitudRepository.findAllById(ids);
        List<AprobacionList> decretadas = new ArrayList<>();

        Decreto nuevoDecreto = new Decreto();
        nuevoDecreto.setRealizadoPor(rut);
        nuevoDecreto.setFechaDecreto(FechaUtils.fechaActual());
        nuevoDecreto.setFechaHoraTransaccion(FechaUtils.getCurrentDateTime());

        Decreto decretoGuardado = decretoRepository.save(nuevoDecreto);

        for (Solicitud solicitud : solicitudes) {
            solicitud.setEstado(Solicitud.EstadoSolicitud.DECRETADA);

            DecretoSolicitud decretoSolicitud = new DecretoSolicitud(decretoGuardado, solicitud);
            decretoSolicitudRepository.save(decretoSolicitud);

            // Map to AprobacionList DTO
            FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(solicitud.getRut());
            AprobacionList dto = mapper.maptoAprobacionList(solicitud, funcionario, nuevoDecreto.getId());
            decretadas.add(dto);
        }

        byte[] generatedDocument = documentoDecretoService.generarDocumento(decretadas, template);
        nuevoDecreto.setDocumentoPdf(generatedDocument);
        decretoRepository.save(nuevoDecreto);

        solicitudRepository.saveAll(solicitudes);

        return decretadas;
    }

    @Override
    @Transactional
    public void revertirDecreto(DecretoDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new IllegalArgumentException("La solicitud de eliminación de decretos no puede ser nula o vacía.");
        }

        for (Long decretoId : request.getIds()) {
            Decreto decreto = decretoRepository.findById(decretoId)
                    .orElseThrow(() -> new NotFounException("Decreto no encontrado con id: " + decretoId));

            // Revertir solicitudes asociadas
            List<Solicitud> solicitudesARevertir = new ArrayList<>();
            for (DecretoSolicitud ds : decreto.getDecretoSolicitudes()) {
                Solicitud solicitud = ds.getSolicitud();
                solicitud.setEstado(EstadoSolicitud.APROBADA);
                solicitudesARevertir.add(solicitud);
            }
            solicitudRepository.saveAll(solicitudesARevertir);

            decretoSolicitudRepository.deleteAll(decreto.getDecretoSolicitudes());

            decretoRepository.delete(decreto);
        }
    }

    @Override
    public List<DecretoDto> findDecretosByFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Decreto> decretos = decretoRepository.findAllByFechaDecretoBetween(fechaInicio, fechaFin);
        return decretos.stream()
                .map(decreto -> new DecretoDto(decreto.getId(), decreto.getFechaDecreto()))
                .toList();
    }

    @Override
    public byte[] getDecretoDocumento(Long id) {
        Decreto decreto = decretoRepository.findById(id)
                .orElseThrow(() -> new NotFounException("Decreto no encontrado con id: " + id));
        return decreto.getDocumentoPdf();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DecretoConSolicitudesDTO> searchDecretos(Long id, LocalDate fechaDesde, LocalDate fechaHasta, Integer rut, Long idSolicitud, String nombreFuncionario, Pageable pageable) {
        Specification<Decreto> spec = buildDecretoSpecification(id, fechaDesde, fechaHasta, rut, idSolicitud, nombreFuncionario);
        Page<Decreto> decretos = decretoRepository.findAll(spec.and((root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.conjunction();
        }), pageable);

        return decretos.map(decreto -> mapDecretoToDto(decreto, rut, idSolicitud));
    }

    private Specification<Decreto> buildDecretoSpecification(Long id, LocalDate fechaDesde, LocalDate fechaHasta, Integer rut, Long idSolicitud, String nombreFuncionario) {
        Specification<Decreto> spec = DecretoSpecification.withDecretoId(id)
            .and(DecretoSpecification.withFechaDecretoBetween(fechaDesde, fechaHasta))
            .and(DecretoSpecification.withRutFuncionario(rut))
            .and(DecretoSpecification.withSolicitudId(idSolicitud));

        if (nombreFuncionario != null && !nombreFuncionario.trim().isEmpty()) {
            java.util.List<Integer> rutsEncontrados = getRutsByFuncionarioName(nombreFuncionario);
            if (!rutsEncontrados.isEmpty()) {
                spec = spec.and(DecretoSpecification.byFuncionarioRutsIn(rutsEncontrados));
            } else {
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.disjunction());
            }
        }
        return spec;
    }

    

    private DecretoConSolicitudesDTO mapDecretoToDto(Decreto decreto, Integer rut, Long idSolicitud) {
        List<SolicitudInfoDTO> solicitudInfos = decreto.getDecretoSolicitudes().stream()
            .filter(decretoSolicitud -> {
                Solicitud solicitud = decretoSolicitud.getSolicitud();
                boolean matchesRut = (rut == null || solicitud.getRut().equals(rut));
                boolean matchesIdSolicitud = (idSolicitud == null || solicitud.getId().equals(idSolicitud));
                return matchesRut && matchesIdSolicitud;
            })
            .map(decretoSolicitud -> {
                Solicitud solicitud = decretoSolicitud.getSolicitud();
                String funcionarioNombreDisplay = "No disponible";
                try {
                    FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(solicitud.getRut());
                    if (funcionario != null) {
                        funcionarioNombreDisplay = funcionario.getNombre() + " " + funcionario.getApellidoPaterno() + " " + funcionario.getApellidoMaterno();
                    }
                } catch (Exception e) {
                    // Si la API de funcionario falla, simplemente no se pone el nombre.
                }
                return new SolicitudInfoDTO(solicitud.getId(), solicitud.getRut(), funcionarioNombreDisplay);
            }).toList();

        return new DecretoConSolicitudesDTO(
            decreto.getId(),
            decreto.getFechaDecreto(),
            solicitudInfos
        );
    }

    private java.util.List<Integer> getRutsByFuncionarioName(String nombreFuncionario) {
        java.util.List<Integer> rutsEncontrados = new java.util.ArrayList<>();
        int pageNumber = 0;
        com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse response;
        do {
            response = apiExtFuncionarioService.buscarFuncionarioByNombre(nombreFuncionario, pageNumber);
            if (response != null && response.getFuncionarios() != null) {
                for (com.newsolicitudes.newsolicitudes.dto.FuncionarioDtoSearch funcionario : response.getFuncionarios()) {
                    if (funcionario.getRut() != null) {
                        rutsEncontrados.add(funcionario.getRut());
                    }
                }
            }
            pageNumber++;
        } while (response != null && response.getCurrentPage() < response.getTotalPages() - 1);
        return rutsEncontrados;
    }
}