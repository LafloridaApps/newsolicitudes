package com.newsolicitudes.newsolicitudes.services.modulo;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.ModuloDto;
import com.newsolicitudes.newsolicitudes.entities.Modulo;

public interface ModuloService {

    Modulo crearModulo(ModuloDto moduloDto);

    List<ModuloDto> listarModulos();

}
