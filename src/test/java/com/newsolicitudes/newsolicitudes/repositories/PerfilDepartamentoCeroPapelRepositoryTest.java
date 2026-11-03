package com.newsolicitudes.newsolicitudes.repositories;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import com.newsolicitudes.newsolicitudes.entities.PerfilDepartamentoCeroPapel;

@SpringBootTest
@Transactional
class PerfilDepartamentoCeroPapelRepositoryTest {
    @Autowired PerfilDepartamentoCeroPapelRepository repository;
    @Autowired EntityManager entityManager;

    @Test void conservaRelacionConfirmadaDespuesDeRecargarDesdeBaseDeDatos() {
        repository.saveAndFlush(new PerfilDepartamentoCeroPapel(8L, 20477389L, "demo"));
        entityManager.clear();
        var relacion = repository.findById(8L).orElseThrow();
        assertEquals(20477389L, relacion.getPerfilId());
        assertEquals("demo", relacion.getAmbiente());
    }
}
