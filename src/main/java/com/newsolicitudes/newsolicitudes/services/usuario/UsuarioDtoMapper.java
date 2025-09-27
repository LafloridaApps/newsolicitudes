package com.newsolicitudes.newsolicitudes.services.usuario;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioDto;
import com.newsolicitudes.newsolicitudes.entities.Usuario;
import com.newsolicitudes.newsolicitudes.services.funcionarioapi.FuncionarioApiService;

@Component
public class UsuarioDtoMapper {

    private final FuncionarioApiService funcionarioResponseApi;

    public UsuarioDtoMapper(FuncionarioApiService funcionarioResponseApi) {
        this.funcionarioResponseApi = funcionarioResponseApi;
    }

    public UsuarioDto mapToUsuarioDto(Usuario usuario) {

        FuncionarioResponseApi funcionarioInfo = funcionarioResponseApi
                .getFuncionarioInfo(Integer.parseInt(usuario.getRut()));
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setIdUsuario(usuario.getId());
        usuarioDto.setRut(funcionarioInfo.getRut());
        usuarioDto.setVrut(funcionarioInfo.getVrut());
        usuarioDto.setLogin(usuario.getLogin());
        usuarioDto.setNombre(funcionarioInfo.getNombreCompleto());
        usuarioDto.setModulos(usuario.getModulos().stream()
                .map(modulo -> new ModuloDto(modulo.getNombre(), modulo.getId()))
                .toList());
        return usuarioDto;
    }

}
