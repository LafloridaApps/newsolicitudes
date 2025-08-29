package com.newsolicitudes.newsolicitudes.services.decretos;

import java.util.Set;

import com.newsolicitudes.newsolicitudes.entities.Decreto;

public interface DecretoService {

    Decreto decretar(Set<Long> ids, Integer rut);

}
