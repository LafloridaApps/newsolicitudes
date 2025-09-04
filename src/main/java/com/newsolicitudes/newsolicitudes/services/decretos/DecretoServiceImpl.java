package com.newsolicitudes.newsolicitudes.services.decretos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.dto.DecretoDeleteRequest; // New import
import com.newsolicitudes.newsolicitudes.dto.DecretoDto; // New import
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

@Service
public class DecretoServiceImpl implements DecretoService {

    private final SolicitudRepository solicitudRepository;
    private final DecretoRepository decretoRepository;
    private final DecretoSolicitudRepository decretoSolicitudRepository;
    private final FuncionarioService funcionarioService;
    private final AprobacionesDecretadasMapper mapper;
    private final DocumentoDecretoService documentoDecretoService;

    public DecretoServiceImpl(SolicitudRepository solicitudRepository, DecretoRepository decretoRepository,
            DecretoSolicitudRepository decretoSolicitudRepository, FuncionarioService funcionarioService,
            AprobacionesDecretadasMapper mapper, DocumentoDecretoService documentoDecretoService) {
        this.solicitudRepository = solicitudRepository;
        this.decretoRepository = decretoRepository;
        this.decretoSolicitudRepository = decretoSolicitudRepository;
        this.funcionarioService = funcionarioService;
        this.mapper = mapper;
        this.documentoDecretoService = documentoDecretoService;
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
            AprobacionList dto = mapper.maptoAprobacionList(solicitud, funcionario);
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
    public void revertirDecreto(DecretoDeleteRequest request) { // Modified signature
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
                solicitud.setEstado(EstadoSolicitud.APROBADA); // Assuming APROBADA is the state to revert to
                solicitudesARevertir.add(solicitud);
            }
            solicitudRepository.saveAll(solicitudesARevertir);

            // Eliminar entradas de DecretoSolicitud
            decretoSolicitudRepository.deleteAll(decreto.getDecretoSolicitudes());

            // Eliminar el decreto
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
}
