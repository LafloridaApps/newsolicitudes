package com.newsolicitudes.newsolicitudes.services.notificacion;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.services.mail.APiMailService;



@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    private final APiMailService apiMailService;

    public NotificacionServiceImpl(APiMailService apiMailService) {
        this.apiMailService = apiMailService;
    }

    @Override
    public void enviarNotificacion(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            apiMailService.enviarMail(to, subject, templateName, templateModel);
        } catch (Exception e) {
            LOGGER.error("Error al enviar notificación a {}: {}", to, e.getMessage(), e);
        }
    }
}
