package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.AusenciaException;
import com.newsolicitudes.newsolicitudes.exceptions.DepartamentoException;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiAusenciasService;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SearchFuncServcie;

@Service
public class SearchFuncServiceImpl implements SearchFuncServcie {

    private final ApiDepartamentoService apiDepartamentoService;
    private final ApiAusenciasService apiAusenciasService;
    private final ApiFuncionarioService apiFuncionarioService;
    private final SubroganciaRepository subroganciaRepository;

    public SearchFuncServiceImpl(ApiDepartamentoService apiDepartamentoService,
            ApiAusenciasService apiAusenciasService, ApiFuncionarioService apiFuncionarioService,
            SubroganciaRepository subroganciaRepository) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.apiAusenciasService = apiAusenciasService;
        this.apiFuncionarioService = apiFuncionarioService;
        this.subroganciaRepository = subroganciaRepository;
    }

    @Override
    public FuncionarioResponse getDirectorActivo(Long idDepartamento, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud) {

        DepartamentoResponse departamento = findDireccion(idDepartamento);

        if (departamento == null) {
            throw new DepartamentoException("No se pudo encontrar una dirección para el departamento con ID: " + idDepartamento);
        }

        FuncionarioResponse director = apiFuncionarioService.obtenerDetalleColaborador(departamento.getRutJefe());

        if (hasAusenciasBetweenDates(director, fechaInicioSolicitud, fechaFinSolicitud)) {
            return findSubrogante(director, fechaInicioSolicitud, fechaFinSolicitud);
        }

        return director;
    }

    private DepartamentoResponse findDireccion(Long idDepartamento) {
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(idDepartamento);

        while (departamento != null && !esDireccion(getNivelDepartamento(departamento))) {
            departamento = apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
        }
        return departamento;
    }

    private FuncionarioResponse findSubrogante(FuncionarioResponse director, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud) {
        Optional<Subrogancia> optSubrogancia = subroganciaRepository
                .findFirstByJefeDepartamentoAndFechaInicioBetween(
                        director.getRut(),
                        fechaInicioSolicitud, fechaFinSolicitud);

        if (optSubrogancia.isPresent()) {
            Subrogancia subrogancia = optSubrogancia.get();
            return apiFuncionarioService.obtenerDetalleColaborador(subrogancia.getSubrogante());
        } else {
            throw new AusenciaException(
                    "No se encontró subrogante para el director con RUT: " + director.getRut() + " en las fechas solicitadas");
        }
    }

    private boolean hasAusenciasBetweenDates(FuncionarioResponse funcionario,
            LocalDate fechaInicioSolicitud, LocalDate fechaFinSolicitud) {

        return !apiAusenciasService.getAusenciasByRutAndFechas(funcionario.getRut(),
                funcionario.getIdent(), fechaInicioSolicitud, fechaFinSolicitud).isEmpty();

    }

    private boolean esDireccion(NivelDepartamento nivelDepartamento) {
        return switch (nivelDepartamento) {
            case DEPARTAMENTO, SECCION, OFICINA -> false;
            case ALCALDIA, DIRECCION, SUBDIRECCION, ADMINISTRACION -> true;

        };
    }

    private NivelDepartamento getNivelDepartamento(DepartamentoResponse departamento) {
        return NivelDepartamento.valueOf(departamento.getNivelDepartamento());
    }

}
