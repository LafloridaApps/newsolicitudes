package com.newsolicitudes.newsolicitudes.services.usuario;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioDto;
import com.newsolicitudes.newsolicitudes.dto.UsuarioModuloRequest;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.entities.Usuario;
import com.newsolicitudes.newsolicitudes.repositories.ModuloRepository;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import com.newsolicitudes.newsolicitudes.services.funcionarioapi.FuncionarioApiService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;
    private final UsuarioDtoMapper usuarioDtoMapper;
    private final FuncionarioApiService funcionarioApiService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, ModuloRepository moduloRepository,
            UsuarioDtoMapper usuarioDtoMapper, FuncionarioApiService funcionarioApiService) {
        this.usuarioRepository = usuarioRepository;
        this.moduloRepository = moduloRepository;
        this.usuarioDtoMapper = usuarioDtoMapper;
        this.funcionarioApiService = funcionarioApiService;
    }

    @Override
    public void guardarUsuarioConModulos(UsuarioModuloRequest request) {
        Usuario usuario = usuarioRepository.findByRut(request.getRut())
                .orElse(new Usuario());
        usuario.setRut(request.getRut());

        Set<Modulo> modulos = new HashSet<>();
        if (request.getModulos() != null) {
            for (Long idModulo : request.getModulos()) {
                Modulo modulo = RepositoryUtils.findOrThrow(moduloRepository.findById(idModulo),
                        String.format("Modulo no encontrado con el id: %s", idModulo));

                modulos.add(modulo);
            }
        }

        usuario.setModulos(modulos);
        usuarioRepository.save(usuario);
    }

    @Override
    public List<ModuloDto> obtenerModulosPorUsuario(String rut) {
        Usuario usuario = RepositoryUtils.findOrThrow(usuarioRepository.findByRut(rut),
                String.format("Usuario no encontrado con RUT: %s", rut));

        return usuario.getModulos().stream()
                .map(modulo -> new ModuloDto(modulo.getNombre(),modulo.getId()))
                .toList();
    }

    @Override
    public UsuarioDto obtenerUsuarioPorRut(String rut) {
        Usuario usuario = RepositoryUtils.findOrThrow(usuarioRepository.findByRut(rut),
                String.format("Usuario no encontrado con RUT: %s", rut));

        return usuarioDtoMapper.mapToUsuarioDto(usuario);

    }

    @Override
    public List<UsuarioDto> obtenerTodosLosUsuarios() {

        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream().map(usuarioDtoMapper::mapToUsuarioDto).toList();

    }

    @Override
    public void createOrUpdateUsuario(String rut, List<Long> moduloIds) {
        
        FuncionarioResponseApi funcionarioInfo = funcionarioApiService.getFuncionarioInfo(Integer.parseInt(rut.split("-")[0]));


        Usuario usuario = usuarioRepository.findByRut(rut)
                .orElse(new Usuario());

        usuario.setRut(rut);
        usuario.setLogin(funcionarioInfo.getRut().toString());

        Set<Modulo> modulos = new HashSet<>();
        if (moduloIds != null) {
            for (Long idModulo : moduloIds) {
                Modulo modulo = RepositoryUtils.findOrThrow(moduloRepository.findById(idModulo),
                        String.format("Modulo no encontrado con el id: %s", idModulo));

                modulos.add(modulo);
            }
        }
        usuario.setModulos(modulos);
        usuarioRepository.save(usuario);
    }

}