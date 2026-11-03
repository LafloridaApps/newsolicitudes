package com.newsolicitudes.newsolicitudes.repositories;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/** Opt-in: requiere configurar explícitamente la conexión a la base local. */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@EnabledIfEnvironmentVariable(named = "RUN_LOCAL_DB_TEST", matches = "true")
@Transactional(readOnly = true)
class SubroganciaLocalDatabaseTest {
    @Autowired SubroganciaRepository repository;

    @Test void consultaRegistrosYColumnasDeIntegracionEnMysql() {
        assertTrue(repository.count() > 0, "Se esperaba una copia local con registros");
        var registros = repository.findAll(PageRequest.of(0, 1));
        assertFalse(registros.isEmpty());
        var subrogancia = registros.getContent().getFirst();
        assertNotNull(subrogancia.getId());
        repository.findBySolicitudId(-1L);
        repository.findByJefeDepartamentoAndIdDeptoAndFechaInicioAndFechaFin(
            subrogancia.getJefeDepartamento(), subrogancia.getIdDepto(),
            subrogancia.getFechaInicio(), subrogancia.getFechaFin());
        if (subrogancia.getJefeDepartamento() != null && subrogancia.getIdDepto() != null
                && subrogancia.getFechaInicio() != null && subrogancia.getFechaFin() != null) {
            assertTrue(repository.existeSubrogancia(subrogancia.getJefeDepartamento(),
                subrogancia.getFechaInicio(), subrogancia.getFechaFin(), subrogancia.getIdDepto()).isPresent());
        }
    }
}
