package com.newsolicitudes.newsolicitudes.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaCreadaResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.services.ceropapel.CeroPapelSubroganciaService;
import com.newsolicitudes.newsolicitudes.services.searchsubrogancia.SubroganciasSearch;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;

class SubroganciaControllerTest {
    @Test void altaLocalExitosaDevuelveAdvertenciaDeEnvioSinFallarLaPeticion() {
        var local = mock(SubroganciaService.class);
        var repository = mock(com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository.class);
        var externo = new CeroPapelSubroganciaService(
                org.springframework.web.reactive.function.client.WebClient.builder(),
                new com.newsolicitudes.newsolicitudes.config.CeroPapelProperties(), repository,
                mock(com.newsolicitudes.newsolicitudes.repositories.PerfilDepartamentoCeroPapelRepository.class),
                mock(com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService.class),
                event -> {}, mock(com.newsolicitudes.newsolicitudes.services.ceropapel.CeroPapelConfiguracion.class));
        var controller = new SubroganciaController(local, mock(SubroganciasSearch.class), externo);
        var request = new SubroganciaRequest();
        request.setFechaInicio(LocalDate.of(2027, 1, 2));
        request.setFechaFin(LocalDate.of(2027, 1, 3));
        request.setIdDepto(10L);
        when(local.createSubrogancia(request, request.getFechaInicio(), request.getFechaFin(), 10L)).thenReturn(7L);
        var registro = new Subrogancia();
        registro.setId(7L);
        registro.setCeroPapelEstado("ERROR");
        registro.setCeroPapelDetalle("CeroPapel no disponible");
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(registro));

        var response = controller.createSubrogancia(request);

        assertEquals(200, response.getStatusCode().value());
        var body = assertInstanceOf(SubroganciaCreadaResponse.class, response.getBody());
        assertEquals(7L, body.id());
        assertEquals("ERROR", body.ceroPapelEstado());
        assertEquals("CeroPapel no disponible", body.ceroPapelDetalle());
        var orden = inOrder(local, repository);
        orden.verify(local).createSubrogancia(request, request.getFechaInicio(), request.getFechaFin(), 10L);
        orden.verify(repository).findById(7L);
    }
}
