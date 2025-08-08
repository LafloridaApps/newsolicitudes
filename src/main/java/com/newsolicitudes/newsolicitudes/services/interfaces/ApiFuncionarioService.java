package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;

public interface ApiFuncionarioService {

    FuncionarioResponse obtenerDetalleColaborador(Integer rut);

    SearchFuncionarioResponse buscarFuncionarioByNombre(String pattern, int pageNmber);

}
