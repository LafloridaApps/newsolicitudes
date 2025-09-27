package com.newsolicitudes.newsolicitudes.services.modulo;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.exceptions.ModuloException;
import com.newsolicitudes.newsolicitudes.repositories.ModuloRepository;

@Service
public class ModuloServiceImpl implements ModuloService {

    private final ModuloRepository moduloRepository;

    public ModuloServiceImpl(ModuloRepository moduloRepository) {
        this.moduloRepository = moduloRepository;
    }

    @Override
    public Modulo crearModulo(ModuloDto moduloDto) {
        moduloRepository.findByNombre(moduloDto.getNombre()).ifPresent(m -> {
            throw new ModuloException("Ya existe un módulo con el nombre: " + moduloDto.getNombre());
        });

        Modulo nuevoModulo = new Modulo();
        nuevoModulo.setNombre(moduloDto.getNombre());

        return moduloRepository.save(nuevoModulo);
    }

    @Override
    public List<ModuloDto> listarModulos() {
        return moduloRepository.findAll()
                .stream()
                .map(modulo -> {
                    ModuloDto dto = new ModuloDto();
                    dto.setId(modulo.getId());
                    dto.setNombre(modulo.getNombre());
                    return dto;
                })
                .sorted(Comparator.comparing(ModuloDto::getId))
                .toList();
    }
}
