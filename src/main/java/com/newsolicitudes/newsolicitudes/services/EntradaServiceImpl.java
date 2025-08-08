package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.EntradaRequest;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.EntradaService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class EntradaServiceImpl implements EntradaService {

    private final EntradaDerivacionRepository entradaRepository;
    private final DerivacionRepository derivacionRepository;

    public EntradaServiceImpl(EntradaDerivacionRepository entradaRepository,
            DerivacionRepository derivacionRepository) {
        this.entradaRepository = entradaRepository;
        this.derivacionRepository = derivacionRepository;
    }

    @Override
    public void createEntrada(EntradaRequest request) {

        Derivacion derivacion = getDerivacionById(request.getIdDerivacion());

        entradaRepository.save(new EntradaDerivacion(derivacion, LocalDate.now(), request.getRutFuncionario()));

    }

    private Derivacion getDerivacionById(Long id) {
        return RepositoryUtils.findOrThrow(derivacionRepository.findById(id),
                String.format("Derivacion %d no existe", id));

    }

}
