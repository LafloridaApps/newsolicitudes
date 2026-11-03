package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.dto.*;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.funcionarioapi.FuncionarioApiService;

@Service
public class SolicitudTercerosServiceImpl implements SolicitudTercerosService {
    private final UsuarioRepository usuarios;
    private final FuncionarioApiService funcionarios;
    private final ApiDepartamentoService departamentos;
    private final ApiExtFuncionarioService busqueda;

    public SolicitudTercerosServiceImpl(UsuarioRepository usuarios, FuncionarioApiService funcionarios,
            ApiDepartamentoService departamentos, ApiExtFuncionarioService busqueda) {
        this.usuarios = usuarios;
        this.funcionarios = funcionarios;
        this.departamentos = departamentos;
        this.busqueda = busqueda;
    }

    private Set<Long> departamentosPermitidos(String rutGestor) {
        var usuario = usuarios.findByRut(rutGestor).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para realizar solicitudes por terceros."));
        if (usuario.getModulos().stream().noneMatch(m -> "SOLICITUD POR TERCEROS".equals(m.getNombre()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para realizar solicitudes por terceros.");
        }
        Integer rut;
        try {
            rut = Integer.valueOf(rutGestor.replace(".", "").split("-")[0].trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT del usuario inválido.");
        }
        var gestor = funcionarios.getFuncionarioInfo(rut);
        if (gestor == null || gestor.getCodDepto() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El usuario no tiene un departamento asignado. No es posible buscar solicitantes.");
        }
        DepartamentoJerarquiaDTO jerarquia;
        try {
            jerarquia = departamentos.getJerarquiaDireccionPorId(gestor.getCodDepto());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo consultar la dirección o subdirección de tu departamento. Intenta nuevamente.");
        }
        if (jerarquia == null || jerarquia.getId() == null
                || !("DIRECCION".equals(jerarquia.getNivel()) || "SUBDIRECCION".equals(jerarquia.getNivel()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo determinar la dirección o subdirección del usuario.");
        }
        Set<Long> permitidos = new HashSet<>();
        agregarDepartamentos(jerarquia, permitidos);
        if (!permitidos.contains(gestor.getCodDepto())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Tu departamento no está incluido en la jerarquía de su dirección o subdirección.");
        }
        return permitidos;
    }

    private void agregarDepartamentos(DepartamentoJerarquiaDTO nodo, Set<Long> ids) {
        if (nodo == null || nodo.getId() == null || !ids.add(nodo.getId())) return;
        if (nodo.getHijos() != null) nodo.getHijos().forEach(hijo -> agregarDepartamentos(hijo, ids));
    }

    @Override
    public FuncionarioResponseApi validarSolicitante(String rutGestor, Integer rut) {
        var permitidos = departamentosPermitidos(rutGestor);
        var persona = funcionarios.getFuncionarioInfo(rut);
        if (persona == null || persona.getRut() == null || !persona.getRut().equals(rut)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron datos para el RUT ingresado.");
        }
        if (persona.getCodDepto() == null || !permitidos.contains(persona.getCodDepto())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes realizar solicitudes para funcionarios de tu dirección.");
        }
        return persona;
    }

    @Override
    public SearchViewFuncionarioResponse buscarPorNombre(String rutGestor, String pattern, int pageNumber) {
        var permitidos = departamentosPermitidos(rutGestor);
        if (pattern == null || pattern.isBlank() || pageNumber < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un nombre y una página válidos.");
        }
        try {
            var resultado = busqueda.buscarFuncionarioViewNombreEnDepartamentos(pattern.trim(), pageNumber, permitidos);
            if (resultado == null || resultado.getFuncionarios() == null) throw new IllegalStateException();
            return resultado;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible consultar funcionarios de tu dirección. Intenta nuevamente.");
        }
    }
}
