package com.newsolicitudes.newsolicitudes.services.solicitud;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Anulacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.SolicitudAnulacion;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.AnulacionException;
import com.newsolicitudes.newsolicitudes.exceptions.NotFoundException;
import com.newsolicitudes.newsolicitudes.repositories.AnulacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudAnulacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.notificacion.NotificacionService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolicitudAnulacionGestorImpl implements SolicitudAnulacionGestor {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudAnulacionGestorImpl.class);

    private final SolicitudAnulacionRepository solicitudAnulacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final NotificacionService notificacionService;
    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;
    private final SubroganciaRepository subroganciaRepository;
    private final AnulacionRepository anulacionRepository;
    private final EntradaDerivacionRepository entradaDerivacionRepository;

    public SolicitudAnulacionGestorImpl(SolicitudAnulacionRepository solicitudAnulacionRepository,
                                        SolicitudRepository solicitudRepository,
                                        NotificacionService notificacionService,
                                        FuncionarioService funcionarioService,
                                        DepartamentoService departamentoService,
                                        SubroganciaRepository subroganciaRepository,
                                        AnulacionRepository anulacionRepository,
                                        EntradaDerivacionRepository entradaDerivacionRepository) {
        this.solicitudAnulacionRepository = solicitudAnulacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.notificacionService = notificacionService;
        this.funcionarioService = funcionarioService;
        this.departamentoService = departamentoService;
        this.subroganciaRepository = subroganciaRepository;
        this.anulacionRepository = anulacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
    }

    @Override
    @Transactional
    public String anular(Solicitud solicitud, String motivo) {
        if (solicitud.getEstado() == Solicitud.EstadoSolicitud.ANULADA) {
            throw new AnulacionException("La solicitud ya se encuentra anulada.");
        }
        if (solicitudAnulacionRepository.existsBySolicitudIdAndEstado(solicitud.getId(), SolicitudAnulacion.EstadoSolicitudAnulacion.PENDIENTE)) {
            throw new AnulacionException("Ya existe una solicitud de anulación pendiente para este formulario.");
        }

        Derivacion derivacionActual = solicitud.getDerivaciones().stream()
                .max(Comparator.comparing(Derivacion::getId))
                .orElse(null);

        boolean tieneRecepcion = solicitud.getDerivaciones().stream()
                .anyMatch(d -> entradaDerivacionRepository.findByDerivacionId(d.getId()).isPresent());

        if (tieneRecepcion) {
            SolicitudAnulacion anulacion = new SolicitudAnulacion();
            anulacion.setSolicitud(solicitud);
            anulacion.setMotivo(motivo);
            anulacion.setFechaSolicitud( FechaUtils.fechaActual());
            anulacion.setEstado(SolicitudAnulacion.EstadoSolicitudAnulacion.PENDIENTE);
            anulacion.setRutSolicitante(solicitud.getRut());
            solicitudAnulacionRepository.save(anulacion);

            // Lógica de notificación al jefe actual
            enviarNotificacionAnulacion(anulacion, derivacionActual);

            return "REQUIERE_APROBACION";
        } else {
            solicitud.setEstado(Solicitud.EstadoSolicitud.ANULADA);
            solicitud.getDerivaciones().forEach(d -> d.setEstadoDerivacion(Derivacion.EstadoDerivacion.ANULADA));
            solicitudRepository.save(solicitud);

            SolicitudAnulacion anulacion = new SolicitudAnulacion();
            anulacion.setSolicitud(solicitud);
            anulacion.setMotivo(motivo);
            anulacion.setFechaSolicitud( FechaUtils.fechaActual());
            anulacion.setEstado(SolicitudAnulacion.EstadoSolicitudAnulacion.APROBADA);
            anulacion.setRutSolicitante(solicitud.getRut());
            solicitudAnulacionRepository.save(anulacion);

            Anulacion anulacionFinal = new Anulacion();
            anulacionFinal.setSolicitudAnulacion(anulacion);
            anulacionFinal.setSolicitud(solicitud);
            anulacionFinal.setRutAprobador(solicitud.getRut()); // Auto-aprobada por el solicitante
            anulacionFinal.setFechaAnulacion( FechaUtils.fechaActual());
            anulacionRepository.save(anulacionFinal);

            enviarNotificacionResolucionAnulacion(anulacion);

            return "ANULADA_DIRECTAMENTE";
        }
    }

    @Override
    @Transactional
    public String resolverAnulacion(Long idSolicitud, Integer rutAprobador) {
        SolicitudAnulacion solicitudAnulacion = solicitudAnulacionRepository
                .findBySolicitudIdAndEstado(idSolicitud, SolicitudAnulacion.EstadoSolicitudAnulacion.PENDIENTE)
                .orElseThrow(() -> new NotFoundException("No se encontró una solicitud de anulación pendiente para el ID: " + idSolicitud));

        solicitudAnulacion.setEstado(SolicitudAnulacion.EstadoSolicitudAnulacion.APROBADA);
        Solicitud solicitud = solicitudAnulacion.getSolicitud();
        solicitud.setEstado(Solicitud.EstadoSolicitud.ANULADA);
        solicitud.getDerivaciones().forEach(d -> d.setEstadoDerivacion(Derivacion.EstadoDerivacion.ANULADA));
        solicitudRepository.save(solicitud);

        Anulacion anulacion = new Anulacion();
        anulacion.setSolicitudAnulacion(solicitudAnulacion);
        anulacion.setSolicitud(solicitud);
        anulacion.setRutAprobador(rutAprobador);
        anulacion.setFechaAnulacion( FechaUtils.fechaActual());
        anulacionRepository.save(anulacion);

        solicitudAnulacionRepository.save(solicitudAnulacion);

        // Lógica de notificación al solicitante original
        enviarNotificacionResolucionAnulacion(solicitudAnulacion);

        return "Anulación aprobada y solicitud actualizada.";
    }

    @Override
    @Transactional
    public String anularDirecto(Solicitud solicitud, String motivo, Integer rutAprobador) {
        if (solicitud.getEstado() == Solicitud.EstadoSolicitud.ANULADA) {
            throw new AnulacionException("La solicitud ya se encuentra anulada.");
        }

        solicitud.setEstado(Solicitud.EstadoSolicitud.ANULADA);
        solicitud.getDerivaciones().forEach(d -> d.setEstadoDerivacion(Derivacion.EstadoDerivacion.ANULADA));
        solicitudRepository.save(solicitud);

        SolicitudAnulacion anulacion = new SolicitudAnulacion();
        anulacion.setSolicitud(solicitud);
        anulacion.setMotivo(motivo);
        anulacion.setFechaSolicitud( FechaUtils.fechaActual());
        anulacion.setEstado(SolicitudAnulacion.EstadoSolicitudAnulacion.APROBADA);
        anulacion.setRutSolicitante(solicitud.getRut());
        solicitudAnulacionRepository.save(anulacion);

        Anulacion anulacionFinal = new Anulacion();
        anulacionFinal.setSolicitudAnulacion(anulacion);
        anulacionFinal.setSolicitud(solicitud);
        anulacionFinal.setRutAprobador(rutAprobador);
        anulacionFinal.setFechaAnulacion( FechaUtils.fechaActual());
        anulacionRepository.save(anulacionFinal);

        enviarNotificacionResolucionAnulacion(anulacion);

        return "Solicitud anulada directamente por administrador.";
    }

    private void enviarNotificacionAnulacion(SolicitudAnulacion anulacion, Derivacion derivacionActual) {
        Solicitud solicitud = anulacion.getSolicitud();
        
        if (derivacionActual == null) {
            throw new AnulacionException("No se encontró una derivación pendiente para notificar la anulación.");
        }

        DepartamentoResponse deptoDestino = departamentoService.getDepartamentoById(derivacionActual.getIdDepto());
        Integer rutJefeDestino = deptoDestino.getRutJefe();
        if (rutJefeDestino == null) {
            logger.warn("Departamento de destino {} no tiene jefe asignado. No se puede notificar anulación.", deptoDestino.getNombre());
            return;
        }

        LocalDate hoy = FechaUtils.fechaActual();
        List<Subrogancia> subrogancias = subroganciaRepository.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeDestino, hoy, hoy);
        
        FuncionarioResponseApi destinatario = !subrogancias.isEmpty()
                ? funcionarioService.getFuncionarioByRut(subrogancias.get(0).getSubrogante())
                : funcionarioService.getFuncionarioByRut(rutJefeDestino);

        FuncionarioResponseApi solicitante = funcionarioService.getFuncionarioByRut(solicitud.getRut());

        Map<String, Object> body = new HashMap<>();
        body.put("nombreJefe", destinatario.getNombreCompleto());
        body.put("nombre", solicitante.getNombreCompleto());
        body.put("motivo", anulacion.getMotivo());
        body.put("tipoPermiso", solicitud.getTipoSolicitud().name());
        body.put("link", "https://appx.laflorida.cl/login");

        try {
            notificacionService.enviarNotificacion(
                    destinatario.getEmail(),
                    "Solicitud de Anulación de " + solicitante.getNombreCompleto(),
                    "anulacion",
                    body
            );
        } catch (Exception e) {
            logger.error("Error al enviar notificación de anulación para solicitud {}: {}", solicitud.getId(), e.getMessage(), e);
        }
    }

    private void enviarNotificacionResolucionAnulacion(SolicitudAnulacion anulacion) {
        FuncionarioResponseApi solicitante = funcionarioService.getFuncionarioByRut(anulacion.getRutSolicitante());

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", solicitante.getNombreCompleto());
        body.put("estado", anulacion.getEstado().name());
        body.put("tipoPermiso", anulacion.getSolicitud().getTipoSolicitud().name());
        body.put("link", "https://appx.laflorida.cl/login");

        String templateName = anulacion.getEstado() == SolicitudAnulacion.EstadoSolicitudAnulacion.APROBADA
                ? "aprobacion-anulacion"
                : "resolucion_anulacion";

        try {
            notificacionService.enviarNotificacion(
                    solicitante.getEmail(),
                    "Resolución de Anulación: " + anulacion.getEstado().name(),
                    templateName,
                    body
            );
        } catch (Exception e) {
            logger.error("Error al enviar notificación de resolución de anulación para solicitud {}: {}", anulacion.getSolicitud().getId(), e.getMessage(), e);
        }
    }
}