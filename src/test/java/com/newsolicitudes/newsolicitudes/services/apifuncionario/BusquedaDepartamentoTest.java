package com.newsolicitudes.newsolicitudes.services.apifuncionario;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import com.newsolicitudes.newsolicitudes.config.ApiProperties;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class BusquedaDepartamentoTest {
    @Test void completaNombresFueraDelHiloReactivoYConsultaCadaDepartamentoUnaVez() {
        var llamadas = new AtomicInteger();
        var departamentos = (ApiDepartamentoService) Proxy.newProxyInstance(
                ApiDepartamentoService.class.getClassLoader(), new Class<?>[]{ApiDepartamentoService.class},
                (proxy, method, args) -> {
                    assertEquals("obtenerDepartamento", method.getName());
                    assertFalse(Schedulers.isInNonBlockingThread());
                    llamadas.incrementAndGet();
                    var departamento = new DepartamentoResponse();
                    departamento.setNombre("Estadio Bicentenario");
                    return departamento;
                });
        var builder = WebClient.builder().exchangeFunction(request -> Mono.fromSupplier(() ->
                ClientResponse.create(HttpStatus.OK).header("Content-Type", "application/json")
                        .body("""
                            {"currentPage":0,"totalPages":1,"totalItems":2,"size":2,"funcionarios":[
                              {"nombreCompleto":"Ana","rut":123,"vrut":"4","codDepto":189},
                              {"nombreCompleto":"Pedro","rut":456,"vrut":"5","codDepto":189}]}
                            """).build()).subscribeOn(Schedulers.parallel()));
        var properties = new ApiProperties();
        properties.setFuncionarioUrl("http://funcionarios.test");
        var service = new ApiExtFuncionarioServiceImpl(builder, properties, departamentos);
        var resultado = service.buscarFuncionarioViewNombreEnDepartamentos("r", 0, Set.of(189L));
        assertEquals(2, resultado.getFuncionarios().size());
        assertTrue(resultado.getFuncionarios().stream()
                .allMatch(persona -> "Estadio Bicentenario".equals(persona.departamento())));
        assertEquals(1, llamadas.get());
    }
}
