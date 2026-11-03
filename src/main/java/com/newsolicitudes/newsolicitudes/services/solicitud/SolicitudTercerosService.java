package com.newsolicitudes.newsolicitudes.services.solicitud;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SearchViewFuncionarioResponse;

public interface SolicitudTercerosService {
    FuncionarioResponseApi validarSolicitante(String rutGestor, Integer rut);
    SearchViewFuncionarioResponse buscarPorNombre(String rutGestor, String pattern, int pageNumber);
}
