package com.newsolicitudes.newsolicitudes.services.aprobacion;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsolicitudes.newsolicitudes.dto.AprobacionRequest;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.exceptions.AprobacionException;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.repositories.VisacionRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.firma.FirmaService;
import com.newsolicitudes.newsolicitudes.services.mapper.PdfDtoMapper;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class AprobacionServiceImpl implements AprobacionService {

    private final AprobacionRepository aprobacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final DerivacionRepository derivacionRepository;
    private final VisacionRepository visacionRepository;
    private final DepartamentoService departamentoService;
    private final SubroganciaRepository subroganciaRepository;
    private final PdfDtoMapper pdfDtoMapper;
    private final FirmaService firmaService;

    public AprobacionServiceImpl(
            AprobacionRepository aprobacionRepository,
            SolicitudRepository solicitudRepository,
            DerivacionRepository derivacionRepository,
            VisacionRepository visacionRepository,
            DepartamentoService departamentoService,
            SubroganciaRepository subroganciaRepository,
            PdfDtoMapper pdfDtoMapper,
            FirmaService firmaService) {
        this.aprobacionRepository = aprobacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.derivacionRepository = derivacionRepository;
        this.visacionRepository = visacionRepository;
        this.departamentoService = departamentoService;
        this.subroganciaRepository = subroganciaRepository;
        this.pdfDtoMapper = pdfDtoMapper;
        this.firmaService = firmaService;
    }

    @Override
    @Transactional
    public void aprobarSolicitud(AprobacionRequest request) {

        Derivacion derivacion = getDerivacionById(request.getIdDerivacion());
        Solicitud solicitud = derivacion.getSolicitud();

        EntradaDerivacion entrada = derivacion.getEntrada();
        if (entrada == null || entrada.getRut() == null) {
            throw new AprobacionException(
                    "No se puede aprobar: la derivación no ha sido recepcionada por un funcionario.");
        }

        if (!shouldSkipVisacion(solicitud, request.getAprobadoPor()) && !verificaVisacion(solicitud)) {
            throw new AprobacionException("El funcionario " + solicitud.getRut()
                    + " no tiene visación para la solicitud " + solicitud.getId());
        }

        derivacion.setEstadoDerivacion(EstadoDerivacion.FINALIZADA);
        derivacionRepository.save(derivacion);

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitudRepository.save(solicitud);

        Aprobacion aprobacion = crearAprobacion(solicitud, request.getAprobadoPor());

        aprobacionRepository.save(aprobacion);

        String url = firmarPdf(solicitud);

        aprobacion.setUrlPdf(url);

        aprobacionRepository.save(aprobacion);

    }

    private String firmarPdf(Solicitud solicitud) {

        return firmaService.firmarPdf(pdfDtoMapper.toPdfDto(solicitud));

    }

    private boolean shouldSkipVisacion(Solicitud solicitud, Integer approverRut) {
        // Condition 1: Approver is a substitute director
        if (isSubrogandoComoDirector(approverRut)) {
            return true;
        }

        // Condition 2: Requester reports to a director
        DepartamentoResponse requesterDepto = departamentoService.getDepartamentoById(solicitud.getIdDepto());
        if (requesterDepto.getIdDeptoSuperior() != null) {
            DepartamentoResponse superiorDepto = departamentoService
                    .getDepartamentoById(requesterDepto.getIdDeptoSuperior());
            NivelDepartamento nivelSuperior = DepartamentoUtils.getNivelDepartamento(superiorDepto);
            if (DepartamentoUtils.tipoPorNivel(nivelSuperior) == TipoDerivacion.FIRMA) {
                return true;
            }
        }

        return false;
    }

    private boolean isSubrogandoComoDirector(Integer rutJefe) {
        List<Subrogancia> subrogancias = subroganciaRepository
                .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, LocalDate.now(),
                        LocalDate.now());
        if (subrogancias.isEmpty()) {
            return false;
        }

        for (Subrogancia subrogancia : subrogancias) {
            DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
            NivelDepartamento nivel = DepartamentoUtils.getNivelDepartamento(deptoSubrogado);
            if (DepartamentoUtils.tipoPorNivel(nivel) == TipoDerivacion.FIRMA) {
                return true;
            }
        }
        return false;
    }

    private boolean verificaVisacion(Solicitud solicitud) {
        return visacionRepository.existsBySolicitud(solicitud);
    }

    private Aprobacion crearAprobacion(Solicitud solicitud, Integer funcionario) {
        Aprobacion aprobacion = new Aprobacion();
        aprobacion.setFechaAprobacion(FechaUtils.fechaActual());
        aprobacion.setSolicitud(solicitud);
        aprobacion.setRut(funcionario);
        return aprobacion;
    }

    private Derivacion getDerivacionById(Long id) {
        return RepositoryUtils.findOrThrow(derivacionRepository.findById(id),
                String.format("No existe la derivación %d", id));
    }
}