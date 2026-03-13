package com.newsolicitudes.newsolicitudes.services.searchfunc;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SearchViewFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;

@Service
public class SearchViewFuncServiceImpl implements SearchViewFuncService {

    private final ApiExtFuncionarioService apiExtFuncionarioService;

    public SearchViewFuncServiceImpl(ApiExtFuncionarioService apiExtFuncionarioService) {
        this.apiExtFuncionarioService = apiExtFuncionarioService;
    }

    @Override
    public SearchViewFuncionarioResponse buscarFuncionarioPorNombre(String patten, int pageNmber) {
        return apiExtFuncionarioService.buscarFuncionarioViewNombre(patten, pageNmber);
    }

}
