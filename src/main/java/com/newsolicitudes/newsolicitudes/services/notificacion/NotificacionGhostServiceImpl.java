package com.newsolicitudes.newsolicitudes.services.notificacion;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class NotificacionGhostServiceImpl implements NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionGhostServiceImpl.class);

    @Override
    public void enviarNotificacion(String to, String subject, String templateName, Map<String, Object> templateModel) {
        logger.info("Correo enviado a :{}", to);
       
    }

}
