package com.newsolicitudes.newsolicitudes.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.entities.Usuario;

@SpringJUnitConfig(SolicitudAutorTest.Config.class)
@Transactional
class AdministracionPaginacionTest {
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ModuloRepository modulos;

    @Test
    void paginaUsuariosConTotalesYBusquedaAntesDelLimite() {
        for (int i = 0; i < 23; i++) {
            Usuario usuario = new Usuario();
            usuario.setRut(String.valueOf(10000000 + i));
            usuario.setLogin("usuario-" + i);
            usuarios.save(usuario);
        }
        var segunda = usuarios.buscarPagina("", List.of(""), PageRequest.of(1, 10, Sort.by("id")));
        assertEquals(23, segunda.getTotalElements());
        assertEquals(3, segunda.getTotalPages());
        assertEquals(10, segunda.getContent().size());
        assertEquals("10000010", segunda.getContent().get(0).getRut());
        var porNombreExterno = usuarios.buscarPagina("nombre externo", List.of("10000022"), PageRequest.of(0, 10));
        assertEquals(1, porNombreExterno.getTotalElements());
        assertEquals("10000022", porNombreExterno.getContent().get(0).getRut());
        assertEquals(1, usuarios.buscarPagina("usuario-22", List.of(""), PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, usuarios.buscarPagina("inexistente", List.of(""), PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void paginaModulosFiltrandoSinDistinguirMayusculas() {
        for (int i = 0; i < 12; i++) {
            Modulo modulo = new Modulo();
            modulo.setNombre("Administración " + i);
            modulos.save(modulo);
        }
        Modulo otro = new Modulo();
        otro.setNombre("Solicitudes");
        modulos.save(otro);
        var pagina = modulos.findByNombreContainingIgnoreCase("ADMINISTRACIÓN", PageRequest.of(1, 10, Sort.by("id")));
        assertEquals(12, pagina.getTotalElements());
        assertEquals(2, pagina.getTotalPages());
        assertEquals(2, pagina.getContent().size());
        assertEquals(0, modulos.findByNombreContainingIgnoreCase("inexistente", PageRequest.of(0, 10)).getTotalElements());
    }
}
