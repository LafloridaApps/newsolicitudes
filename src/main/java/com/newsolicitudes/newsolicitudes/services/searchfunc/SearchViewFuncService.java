package com.newsolicitudes.newsolicitudes.services.searchfunc;

import com.newsolicitudes.newsolicitudes.dto.SearchViewFuncionarioResponse;

public interface SearchViewFuncService {

    SearchViewFuncionarioResponse buscarFuncionarioPorNombre(String patten, int pageNmber);

}
