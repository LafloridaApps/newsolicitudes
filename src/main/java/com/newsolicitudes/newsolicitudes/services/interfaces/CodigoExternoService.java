package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.entities.Departamento;

public interface CodigoExternoService {

    Departamento findByCodigoEx(String codigoEx);
}
