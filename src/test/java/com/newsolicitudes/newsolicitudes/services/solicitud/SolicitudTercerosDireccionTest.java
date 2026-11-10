package com.newsolicitudes.newsolicitudes.services.solicitud;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.dto.*;
import com.newsolicitudes.newsolicitudes.entities.*;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.funcionarioapi.FuncionarioApiService;

class SolicitudTercerosDireccionTest {
    private final UsuarioRepository usuarios = proxy(UsuarioRepository.class);
    private final FuncionarioApiService funcionarios = proxy(FuncionarioApiService.class);
    private final ApiDepartamentoService departamentos = proxy(ApiDepartamentoService.class);
    private final ApiExtFuncionarioService busqueda = proxy(ApiExtFuncionarioService.class);
    private final SolicitudTercerosService service = new SolicitudTercerosServiceImpl(usuarios, funcionarios, departamentos, busqueda);

    private static <T> T proxy(Class<T> tipo) { return mock(tipo, withSettings().mockMaker("mock-maker-proxy")); }
    private FuncionarioResponseApi persona(int rut, Long depto) {
        var p = new FuncionarioResponseApi(); p.setRut(rut); p.setCodDepto(depto); return p;
    }
    @BeforeEach
    void preparar() {
        var usuario = new Usuario(); usuario.setModulos(Set.of(new Modulo("SOLICITUD POR TERCEROS")));
        when(usuarios.findByRut("123")).thenReturn(Optional.of(usuario));
        when(funcionarios.getFuncionarioInfo(123)).thenReturn(persona(123, 30L));
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(new DepartamentoJerarquiaDTO(10L, "Dirección", "DIRECCION", List.of(
            new DepartamentoJerarquiaDTO(20L, "Departamento", "DEPARTAMENTO", List.of(
                new DepartamentoJerarquiaDTO(30L, "Oficina", "OFICINA", List.of()))),
            new DepartamentoJerarquiaDTO(40L, "Sección", "SECCION", List.of()))));
    }
    @Test void permiteOtroDepartamentoDeLaMismaDireccion() {
        var destino = persona(456, 40L); when(funcionarios.getFuncionarioInfo(456)).thenReturn(destino);
        assertSame(destino, service.validarSolicitante("123", 456));
    }
    @Test void completaDepartamentoDelSolicitanteCuandoElDetalleNoLoIncluye() {
        var destino = persona(456, 40L);
        when(funcionarios.getFuncionarioInfo(456)).thenReturn(destino);
        var departamento = new DepartamentoResponse();
        departamento.setNombre("Sección de Administración");
        when(departamentos.obtenerDepartamento(40L)).thenReturn(departamento);
        assertEquals("Sección de Administración", service.validarSolicitante("123", 456).getDepartamento());
    }
    @Test void bloqueaOtraDireccion() {
        when(funcionarios.getFuncionarioInfo(456)).thenReturn(persona(456, 99L));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.validarSolicitante("123", 456)).getStatusCode().value());
    }
    @Test void bloqueaJerarquiaNoDisponible() {
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(null);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.validarSolicitante("123", 456)).getStatusCode().value());
        verify(funcionarios, never()).getFuncionarioInfo(456);
    }
    @Test void bloqueaJerarquiaQueNoIncluyeElDepartamentoDelUsuario() {
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(
                new DepartamentoJerarquiaDTO(10L, "Dirección", "DIRECCION", List.of()));
        assertThrows(ResponseStatusException.class, () -> service.validarSolicitante("123", 456));
        verifyNoInteractions(busqueda);
    }
    @Test void permiteAmbitoSubdireccionSinDireccionSuperior() {
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(
                new DepartamentoJerarquiaDTO(20L, "Subdirección", "SUBDIRECCION", List.of(
                    new DepartamentoJerarquiaDTO(30L, "Oficina", "OFICINA", List.of()))));
        var destino = persona(456, 20L);
        when(funcionarios.getFuncionarioInfo(456)).thenReturn(destino);
        assertSame(destino, service.validarSolicitante("123", 456));
    }
    @Test void permiteTodaAdministracionMunicipal() {
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(
                new DepartamentoJerarquiaDTO(2L, "Administración Municipal", "ADMINISTRACION", List.of(
                    new DepartamentoJerarquiaDTO(30L, "Estadio", "DEPARTAMENTO", List.of()),
                    new DepartamentoJerarquiaDTO(40L, "Otra unidad", "DEPARTAMENTO", List.of()))));
        var destino = persona(456, 40L);
        when(funcionarios.getFuncionarioInfo(456)).thenReturn(destino);
        assertSame(destino, service.validarSolicitante("123", 456));
        var resultado = new SearchViewFuncionarioResponse(); resultado.setFuncionarios(List.of());
        when(busqueda.buscarFuncionarioViewNombreEnDepartamentos("ross", 0, Set.of(2L, 30L, 40L))).thenReturn(resultado);
        assertSame(resultado, service.buscarPorNombre("123", "ross", 0));
    }
    @Test void alcaldiaExcluyeAdministracionYSusDirecciones() {
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(
                new DepartamentoJerarquiaDTO(1L, "Alcaldía", "ALCALDIA", List.of(
                    new DepartamentoJerarquiaDTO(30L, "Unidad directa", "DEPARTAMENTO", List.of()),
                    new DepartamentoJerarquiaDTO(2L, "Administración", "ADMINISTRACION", List.of(
                        new DepartamentoJerarquiaDTO(40L, "Dirección", "DIRECCION", List.of()))))));
        when(funcionarios.getFuncionarioInfo(456)).thenReturn(persona(456, 40L));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.validarSolicitante("123", 456)).getStatusCode().value());
        when(funcionarios.getFuncionarioInfo(457)).thenReturn(persona(457, 2L));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.validarSolicitante("123", 457)).getStatusCode().value());
        var resultado = new SearchViewFuncionarioResponse(); resultado.setFuncionarios(List.of());
        when(busqueda.buscarFuncionarioViewNombreEnDepartamentos("ross", 0, Set.of(1L, 30L))).thenReturn(resultado);
        assertSame(resultado, service.buscarPorNombre("123", "ross", 0));
        when(funcionarios.getFuncionarioInfo(789)).thenReturn(persona(789, 99L));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.validarSolicitante("123", 789)).getStatusCode().value());
    }
    @Test void administracionExcluyeDireccionesAunqueLaApiDevuelvaElOrganigramaCompleto() {
        when(departamentos.getJerarquiaDireccionPorId(30L)).thenReturn(
                new DepartamentoJerarquiaDTO(2L, "Administración", "ADMINISTRACION", List.of(
                    new DepartamentoJerarquiaDTO(30L, "Estadio", "DEPARTAMENTO", List.of(
                        new DepartamentoJerarquiaDTO(31L, "Sección propia", "SECCION", List.of()))),
                    new DepartamentoJerarquiaDTO(10L, "Obras", "DIRECCION", List.of(
                        new DepartamentoJerarquiaDTO(11L, "Departamento de Obras", "DEPARTAMENTO", List.of()))))));
        var resultado = new SearchViewFuncionarioResponse(); resultado.setFuncionarios(List.of());
        when(busqueda.buscarFuncionarioViewNombreEnDepartamentos("r", 0, Set.of(2L, 30L, 31L))).thenReturn(resultado);
        assertSame(resultado, service.buscarPorNombre("123", "r", 0));
        for (long depto : new long[]{10L, 11L}) {
            when(funcionarios.getFuncionarioInfo(456)).thenReturn(persona(456, depto));
            assertEquals(403, assertThrows(ResponseStatusException.class,
                    () -> service.validarSolicitante("123", 456)).getStatusCode().value());
        }
        var propia = persona(457, 31L);
        when(funcionarios.getFuncionarioInfo(457)).thenReturn(propia);
        assertSame(propia, service.validarSolicitante("123", 457));
    }
    @Test void filtraAntesDePaginar() {
        var resultado = new SearchViewFuncionarioResponse(); resultado.setFuncionarios(List.of());
        when(busqueda.buscarFuncionarioViewNombreEnDepartamentos("Ana", 2, Set.of(10L,20L,30L,40L))).thenReturn(resultado);
        assertSame(resultado, service.buscarPorNombre("123", " Ana ", 2));
        verify(busqueda).buscarFuncionarioViewNombreEnDepartamentos("Ana", 2, Set.of(10L,20L,30L,40L));
    }
    @Test void bloqueaUsuarioSinPermiso() {
        when(usuarios.findByRut("123")).thenReturn(Optional.of(new Usuario()));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.buscarPorNombre("123", "Ana", 0)).getStatusCode().value());
        verifyNoInteractions(funcionarios, departamentos, busqueda);
    }
    @Test void informaRutInexistente() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.validarSolicitante("123", 456)).getStatusCode().value());
    }
}
