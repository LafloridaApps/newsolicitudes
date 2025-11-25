package com.newsolicitudes.newsolicitudes.services.notificacion;

import java.util.Map;

public interface NotificacionService {
    void enviarNotificacion(String to, String subject, String templateName, Map<String, Object> templateModel);
}
