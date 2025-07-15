package com.newsolicitudes.newsolicitudes.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.entities.Feriado;
import com.newsolicitudes.newsolicitudes.repositories.FeriadoRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.FeriadoService;

@Service
public class FeriadoServiceImpl implements FeriadoService {

    private final FeriadoRepository feriadoRepository;

    public FeriadoServiceImpl(FeriadoRepository feriadoRepository) {
        this.feriadoRepository = feriadoRepository;
    }

    @Override
    public List<Feriado> getAll() {
        return feriadoRepository.findAll();
    }

}
