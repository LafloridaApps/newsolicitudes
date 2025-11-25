package com.newsolicitudes.newsolicitudes.services.notificacion;

import java.util.Map;
import org.springframework.stereotype.Service;
import com.newsolicitudes.newsolicitudes.services.mail.ApiMailService;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final ApiMailService apiMailService;

    public NotificacionServiceImpl(ApiMailService apiMailService) {
        this.apiMailService = apiMailService;
    }

    @Override
    public void enviarNotificacion(String to, String subject, String templateName, Map<String, Object> templateModel) {
        apiMailService.enviarMail(to, subject, templateName, templateModel);
    }
}
