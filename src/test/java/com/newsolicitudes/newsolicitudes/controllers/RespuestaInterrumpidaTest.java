package com.newsolicitudes.newsolicitudes.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import com.newsolicitudes.newsolicitudes.controllers.handlerexceptions.HandlerExceptions;

class RespuestaInterrumpidaTest {
    @RestController
    static class Controlador {
        @GetMapping("/prueba-desconexion")
        public String desconexion() throws AsyncRequestNotUsableException {
            throw new AsyncRequestNotUsableException("Conexión cerrada");
        }

        @GetMapping("/prueba-io")
        public String errorIo() throws IOException {
            throw new IOException("Error de archivo");
        }
    }

    @Test
    void conexionCerradaNoIntentaEscribirOtroJson() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Controlador())
                .setControllerAdvice(new HandlerExceptions()).build();
        mvc.perform(get("/prueba-desconexion"))
                .andExpect(content().string(""));
    }

    @Test
    void otrosErroresIoConservanSuManejo() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Controlador())
                .setControllerAdvice(new HandlerExceptions()).build();
        mvc.perform(get("/prueba-io"))
                .andExpect(status().isConflict());
    }
}
