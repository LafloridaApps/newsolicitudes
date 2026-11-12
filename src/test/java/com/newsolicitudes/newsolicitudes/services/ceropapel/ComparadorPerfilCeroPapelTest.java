package com.newsolicitudes.newsolicitudes.services.ceropapel;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.newsolicitudes.newsolicitudes.services.ceropapel.ComparadorPerfilCeroPapel.Perfil;

class ComparadorPerfilCeroPapelTest {
    @Test void diferenciaAdministradorYDirectorPorNombreDeUnidad() {
        var perfiles = List.of(
            new Perfil(20509518L, "Administrador sistema", "Administradores de Sistema"),
            new Perfil(20477389L, "Director de Dirección de Tecnologías de la Información", "Direccion de Tecnologias de la Informacion"));
        assertEquals(20477389L, ComparadorPerfilCeroPapel.comparar(" DIRECCIÓN  DE TECNOLOGÍAS DE LA INFORMACIÓN ", perfiles));
    }
    @Test void dosCargosEnMismaUnidadRequierenSeleccion() {
        assertNull(ComparadorPerfilCeroPapel.comparar("Dirección de Salud", List.of(
            new Perfil(1L, "Director", "Direccion de Salud"), new Perfil(2L, "Secretaria", "Dirección de Salud"))));
    }
    @Test void sinUnidadUsaFraseCompletaDelCargo() {
        assertEquals(1L, ComparadorPerfilCeroPapel.comparar("Dirección de Salud", List.of(
            new Perfil(1L, "Director de Dirección de Salud", ""), new Perfil(2L, "Administrador", ""))));
    }
    @Test void coincidenciasParcialesOUnidadContradictoriaNoSeleccionan() {
        assertNull(ComparadorPerfilCeroPapel.comparar("Salud", List.of(new Perfil(1L, "Jefe de Salud Ambiental", ""))));
        assertNull(ComparadorPerfilCeroPapel.comparar("Dirección de Salud", List.of(new Perfil(1L, "Director de Dirección de Salud", "Otra unidad"))));
        assertNull(ComparadorPerfilCeroPapel.comparar(null, List.of(new Perfil(1L, "Director", ""))));
    }
    @Test void reconoceAbreviaturaRealDelDepartamentoLocalSinConfundirAdministrador() {
        var perfiles = List.of(
            new Perfil(20509518L, "Administrador sistema", "Administradores de Sistema"),
            new Perfil(20477389L, "Director de Dirección de Tecnologías de la Información", "Direccion de Tecnologias de la Informacion"));
        assertEquals(20477389L, ComparadorPerfilCeroPapel.comparar("Direc. Tecnologias de la Informacion", perfiles));
        assertNull(ComparadorPerfilCeroPapel.comparar("Direc. de Salud", perfiles));
    }

}
