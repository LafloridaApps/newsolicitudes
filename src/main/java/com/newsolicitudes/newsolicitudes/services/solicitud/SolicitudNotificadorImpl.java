package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.config.AppProperties;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.exceptions.NotificacionException;
import com.newsolicitudes.newsolicitudes.services.notificacion.NotificacionService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

@Service
public class SolicitudNotificadorImpl implements SolicitudNotificador {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudNotificadorImpl.class);

    private final NotificacionService notificacionService;
    private final FuncionarioService funcionarioService;
    private final SubroganciaRepository subroganciaRepository;
    private final AppProperties appProperties;

    public SolicitudNotificadorImpl(NotificacionService notificacionService,
            FuncionarioService funcionarioService,
            SubroganciaRepository subroganciaRepository,
            AppProperties appProperties) {
        this.notificacionService = notificacionService;
        this.funcionarioService = funcionarioService;
        this.subroganciaRepository = subroganciaRepository;
        this.appProperties = appProperties;

    }

    @Override
    public void enviarNotificacionNuevaSolicitud(Solicitud solicitud,
            DepartamentoResponse departamentoDestino,
            FuncionarioResponseApi funcionario,
            String nombreDepartamentoActual) {

        try {
            Integer rutJefeDestino = departamentoDestino.getRutJefe();
            if (rutJefeDestino == null) {
                logger.warn("El departamento destino ({}) no tiene jefe asignado. No se enviará notificación.",
                        departamentoDestino.getNombre());
                return; // No se puede notificar si el departamento de destino no tiene jefe asignado
            }

            LocalDate hoy = FechaUtils.fechaActual();
            List<Subrogancia> subrogancias = subroganciaRepository
                    .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeDestino, hoy,
                            hoy);

            FuncionarioResponseApi destinatario = !subrogancias.isEmpty()
                    ? funcionarioService.getFuncionarioByRut(subrogancias.get(0).getSubrogante())
                    : funcionarioService.getFuncionarioByRut(rutJefeDestino);

            Map<String, Object> body = new HashMap<>();
            body.put("nombreJefe", destinatario.getNombreCompleto());
            body.put("nombre", funcionario.getNombreCompleto());
            body.put("tipoPermiso", solicitud.getTipoSolicitud().name());
            body.put("departamento", nombreDepartamentoActual);
            body.put("link", appProperties.getIntranetUrl());
            body.put("idSolicitud", solicitud.getId());

            notificacionService.enviarNotificacion(
                    destinatario.getEmail(),
                    String.format("Nueva Solicitud de %s", funcionario.getNombreCompleto()),
                    "solicitud",
                    body);
        } catch (Exception e) {
            logger.error("Error al enviar notificación de nueva solicitud. Funcionario: {}, Destino: {}",
                    funcionario.getRut(),
                    departamentoDestino.getId(),
                    e);
            throw new NotificacionException("Error al enviar notificacion de nueva solicitud", e);
        }
    }
}