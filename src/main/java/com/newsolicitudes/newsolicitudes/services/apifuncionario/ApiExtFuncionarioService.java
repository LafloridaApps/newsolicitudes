package com.newsolicitudes.newsolicitudes.services.apifuncionario;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;

public interface ApiExtFuncionarioService {

    FuncionarioResponseApi obtenerDetalleColaborador(Integer rut);

    SearchFuncionarioResponse buscarFuncionarioByNombre(String pattern, int pageNmber);

}
