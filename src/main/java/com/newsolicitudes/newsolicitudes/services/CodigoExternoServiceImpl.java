package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.entities.CodigoExterno;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.repositories.CodigoExternoRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.CodigoExternoService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class CodigoExternoServiceImpl implements CodigoExternoService {

    private CodigoExternoRepository codigoExternoRepository;

    public CodigoExternoServiceImpl(CodigoExternoRepository codigoExternoRepository) {
        this.codigoExternoRepository = codigoExternoRepository;
    }

    @Override
    public Departamento findByCodigoEx(String codigoEx) {

        CodigoExterno codigoExterno = RepositoryUtils.findOrThrow(codigoExternoRepository.findByCodigoEx(codigoEx),
                String.format("No existe el departamento %s", codigoEx));

        return codigoExterno.getDepartamento();

    }

}
