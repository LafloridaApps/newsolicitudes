package com.newsolicitudes.newsolicitudes.services.noleidas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

@Service
public class NoLeidasServiceImpl implements NoLeidasService {

    private final DerivacionRepository derivacionRepository;
    private final EntradaDerivacionRepository entradaDerivacionRepository;
    private final SubroganciaRepository subroganciaRepository;
    private final DepartamentoService departamentoService;

    public NoLeidasServiceImpl(DerivacionRepository derivacionRepository,
            EntradaDerivacionRepository entradaDerivacionRepository,
            SubroganciaRepository subroganciaRepository,
            DepartamentoService departamentoService) {
        this.derivacionRepository = derivacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
        this.subroganciaRepository = subroganciaRepository;
        this.departamentoService = departamentoService;
    }

    @Override
    public long cantidadNoLeidas(Long depto) {
        DepartamentoResponse deptoResponse = departamentoService.getDepartamentoById(depto);
        LocalDate hoy = FechaUtils.fechaActual();
        List<Subrogancia> subrogancias = subroganciaRepository
                .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(deptoResponse.getRutJefe(), hoy, hoy);

        List<Long> deptoIds = new ArrayList<>();
        deptoIds.add(depto);
        if (!subrogancias.isEmpty()) {
            deptoIds.addAll(subrogancias.stream().map(Subrogancia::getIdDepto).toList());
        }

        List<Derivacion> derivaciones = derivacionRepository.findByIdDeptoIn(deptoIds);

        long cantidad = derivaciones.stream()
                .filter(derivacion -> !hasEntrada(derivacion))
                .count();

        return cantidad >= 0 ? cantidad : 0;
    }

    private boolean hasEntrada(Derivacion derivacion) {
        return entradaDerivacionRepository.findByDerivacionId(derivacion.getId()).isPresent();
    }

}
