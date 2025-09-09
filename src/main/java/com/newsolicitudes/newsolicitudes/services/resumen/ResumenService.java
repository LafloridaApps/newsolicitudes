package com.newsolicitudes.newsolicitudes.services.resumen;

import com.newsolicitudes.newsolicitudes.dto.ResumenJefeDepartamentoDTO;

public interface ResumenService {
    ResumenJefeDepartamentoDTO getResumenJefeDepartamento(Integer rutJefe, Long idDepartamento);
}