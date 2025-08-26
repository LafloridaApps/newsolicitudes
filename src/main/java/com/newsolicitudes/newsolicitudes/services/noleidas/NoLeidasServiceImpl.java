package com.newsolicitudes.newsolicitudes.services.noleidas;

import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;

@Service
public class NoLeidasServiceImpl implements NoLeidasService {

    private final DerivacionRepository derivacionRepository;

    private final EntradaDerivacionRepository entradaDerivacionRepository;

    public NoLeidasServiceImpl(DerivacionRepository derivacionRepository,
            EntradaDerivacionRepository entradaDerivacionRepository) {
        this.derivacionRepository = derivacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
    }

    @Override
    public long cantidadNoLeidas(Long depto) {

        List<Derivacion> derivaciones = derivacionRepository.findByIdDepto(depto);

        long cantidad = derivaciones.stream()
                .filter(derivacion -> !hasEntrada(derivacion))
                .count();

        return cantidad >= 0 ? cantidad : 0;

    }

    private boolean hasEntrada(Derivacion derivacion) {
        return entradaDerivacionRepository.findByDerivacionId(derivacion.getId()).isPresent();
    }

}
