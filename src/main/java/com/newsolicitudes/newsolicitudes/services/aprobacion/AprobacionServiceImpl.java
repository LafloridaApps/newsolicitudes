package com.newsolicitudes.newsolicitudes.services.aprobacion;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.newsolicitudes.newsolicitudes.exceptions.SolicitudException;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.repositories.VisacionRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.firma.FirmaService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.mail.ApiMailService;
import com.newsolicitudes.newsolicitudes.services.mapper.PdfDtoMapper;
import com.newsolicitudes.newsolicitudes.services.solicitud.SolicitudService;
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
    private final FuncionarioService funcionarioService;
    private final ApiMailService apiMailService;
    private final SolicitudService solicitudService;
    private static final String URL_LOGIN = "https://appx.laflorida.cl/login";
    private static final String SUBJECT_MAIL = "Aprobación de Solicitud";
    private static final String TEMPLATE_MAIL = "aprobacion";
    private static final String ERROR_MESSAGE_FIRMA = "No se puede firmar la solicitud. Su firma podría no estar vigente o existe un error de conexión";

    public AprobacionServiceImpl(
            AprobacionRepository aprobacionRepository,
            SolicitudRepository solicitudRepository,
            DerivacionRepository derivacionRepository,
            VisacionRepository visacionRepository,
            DepartamentoService departamentoService,
            SubroganciaRepository subroganciaRepository,
            PdfDtoMapper pdfDtoMapper,
            FirmaService firmaService,
            FuncionarioService funcionarioService,
            ApiMailService apiMailService,
            SolicitudService solicitudService) {
        this.aprobacionRepository = aprobacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.derivacionRepository = derivacionRepository;
        this.visacionRepository = visacionRepository;
        this.departamentoService = departamentoService;
        this.subroganciaRepository = subroganciaRepository;
        this.pdfDtoMapper = pdfDtoMapper;
        this.firmaService = firmaService;
        this.funcionarioService = funcionarioService;
        this.apiMailService = apiMailService;
        this.solicitudService = solicitudService;
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

        if (!shouldSkipVisacion(solicitud, request.getAprobadoPor(), derivacion) && !verificaVisacion(solicitud)) {
            throw new AprobacionException("El funcionario " + solicitud.getRut()
                    + " no tiene visación para la solicitud " + solicitud.getId());
        }

        if (solicitudService.buscarSolicitudesPendientesAprobacion(solicitud.getTipoSolicitud().toString(),
                solicitud.getRut())) {
            throw new SolicitudException("El funcionario " + solicitud.getRut()
                    + " tiene solicitudes pendientes de aprobación del mismo tipo.");
        }

        derivacion.setEstadoDerivacion(EstadoDerivacion.FINALIZADA);
        derivacionRepository.save(derivacion);

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitudRepository.save(solicitud);

        Aprobacion aprobacion = crearAprobacion(solicitud, request.getAprobadoPor());

        aprobacionRepository.save(aprobacion);

        String url = firmarPdf(solicitud);

        validarUrl(url);

        aprobacion.setUrlPdf(url);

        aprobacionRepository.save(aprobacion);

        if (validarCorreo(funcionarioService.getFuncionarioByRut(solicitud.getRut()).getEmail())) {
            sendMail(solicitud.getRut(), solicitud.getId());
        }

    }

    private void validarUrl(String url) {
        if (url == null) {
            throw new AprobacionException(
                    ERROR_MESSAGE_FIRMA);
        }
        if (url.contains("ERROR") || url.contains("404")) {
            throw new AprobacionException(
                    ERROR_MESSAGE_FIRMA);
        }
    }

    private void sendMail(Integer rutSolicitante, Long idSolicitud) {

        String to = funcionarioService.getFuncionarioByRut(rutSolicitante).getEmail();
        String subject = SUBJECT_MAIL;
        String templateName = TEMPLATE_MAIL;
        Map<String, Object> body = new HashMap<>();
        body.put("link", URL_LOGIN);
        body.put("idSolicitud", idSolicitud);

        apiMailService.enviarMail(to, subject, templateName, body);
    }

    private String firmarPdf(Solicitud solicitud) {

        return firmaService.firmarPdf(pdfDtoMapper.toPdfDto(solicitud));

    }

    private boolean validarCorreo(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    private boolean shouldSkipVisacion(Solicitud solicitud, Integer approverRut, Derivacion derivacion) {
        if (derivacion.getTipo() == TipoDerivacion.FIRMA) {
            return true;
        }
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

    @Override
    public void repairUrl(Long idSolicitud) {

        Solicitud solicitud = RepositoryUtils.findOrThrow(solicitudRepository.findById(idSolicitud),
                String.format("No existe la solicitud %d", idSolicitud));

        Aprobacion aprobacion = aprobacionRepository.findBySolicitud(solicitud)
                .orElseThrow(() -> new AprobacionException("No existe la aprobación para la solicitud " + idSolicitud));

        aprobacion.setUrlPdf(null);

        String nuevaUrl = firmaService.firmarPdf(pdfDtoMapper.toPdfDto(solicitud));

        validarUrl(nuevaUrl);

        aprobacion.setUrlPdf(nuevaUrl);

        aprobacionRepository.save(aprobacion);

    }

}