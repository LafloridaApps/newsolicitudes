package com.newsolicitudes.newsolicitudes.services.notificacion;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.newsolicitudes.newsolicitudes.services.mail.APiMailService;

class NotificacionServiceSelectionTest {

    @Test
    void seleccionaGhostSinEnviarCorreosReales() {
        APiMailService mailService = mock(APiMailService.class);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(APiMailService.class, () -> mailService);
            context.register(NotificacionServiceImpl.class, NotificacionGhostServiceImpl.class);
            context.refresh();

            NotificacionService service = context.getBean(NotificacionService.class);
            assertSame(context.getBean(NotificacionGhostServiceImpl.class), service);

            Map<String, Object> body = Map.of("idSolicitud", 1L);
            service.enviarNotificacion("destino@example.com", "Aprobación", "aprobacion", body);
            verifyNoInteractions(mailService);
        }
    }
}
