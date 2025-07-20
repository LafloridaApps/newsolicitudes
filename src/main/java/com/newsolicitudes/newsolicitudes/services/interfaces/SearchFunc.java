package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioPageResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;

public interface SearchFunc {

    FuncionarioPageResponse searchFuncionario(String nombre, Long idDepartamento);

    FuncionarioResponse getDirectorActivo(Long idDepartamento);

}
