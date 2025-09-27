package com.newsolicitudes.newsolicitudes.services.usuario;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioModuloRequest;

public interface UsuarioService {

    void guardarUsuarioConModulos(UsuarioModuloRequest request);

    List<ModuloDto> obtenerModulosPorUsuario(String rut);

    UsuarioDto obtenerUsuarioPorRut(String rut);

    List<UsuarioDto> obtenerTodosLosUsuarios();

    void createOrUpdateUsuario(String rut, List<Long> moduloIds);

}
