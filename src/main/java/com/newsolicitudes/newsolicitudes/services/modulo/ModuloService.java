package com.newsolicitudes.newsolicitudes.services.modulo;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.entities.Modulo;

public interface ModuloService {

    Page<ModuloDto> listarPagina(String busqueda, Pageable pageable);

    Modulo crearModulo(ModuloDto moduloDto);

    List<ModuloDto> listarModulos();

}
