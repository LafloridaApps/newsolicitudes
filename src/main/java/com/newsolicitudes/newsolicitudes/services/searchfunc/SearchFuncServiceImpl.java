package com.newsolicitudes.newsolicitudes.services.searchfunc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.AusenciaException;
import com.newsolicitudes.newsolicitudes.exceptions.DepartamentoException;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.apiausencias.ApiAusenciasService;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiFuncionarioService;

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
            throw new DepartamentoException(
                    "No se pudo encontrar una dirección para el departamento con ID: " + idDepartamento);
        }

        FuncionarioResponse director = buscarFuncionarioByRut(departamento.getRutJefe());

        if (!hasAusenciasBetweenDates(director, fechaInicioSolicitud, fechaFinSolicitud)) {
            director =  findSubrogante(director, fechaInicioSolicitud, fechaFinSolicitud);
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
            return buscarFuncionarioByRut(subrogancia.getSubrogante());
        } else {
            return director;
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

    private FuncionarioResponse buscarFuncionarioByRut(Integer rut) {
        return apiFuncionarioService.obtenerDetalleColaborador(rut);
    }

    @Override
    public FuncionarioResponse buscarSubroganteByRut(Integer rut, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud) {

        FuncionarioResponse funcionario = buscarFuncionarioByRut(rut);

        if (hasAusenciasBetweenDates(funcionario, fechaInicioSolicitud, fechaFinSolicitud)) {
            throw new AusenciaException(
                    "El funcionario con RUT: " + rut + " tiene ausencias en las fechas solicitadas");
        }

        return funcionario;

    }

    @Override
    public List<FuncionarioResponse> buscarFuncionarioByNombre(String pattern, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud, int pageNmber, Long iddepto) {

        SearchFuncionarioResponse searchFuncionarioResponse = buscarFuncionarioByNombre(pattern, pageNmber);

        List<DepartamentoResponse> departamentoFamilia = apiDepartamentoService
                .obtenerFamiliaDepto(iddepto);

        List<Long> familiaIds = flattenDepartamentos(departamentoFamilia);

        return searchFuncionarioResponse.getFuncionarios().stream().map(f -> buscarFuncionarioByRut(f.getRut()))
                .filter(f -> familiaIds.contains(f.getCodDepto()))
                .filter(a -> !hasAusenciasBetweenDates(a, fechaInicioSolicitud, fechaFinSolicitud))
                .toList();

    }

    private List<Long> flattenDepartamentos(List<DepartamentoResponse> departamentos) {
        List<Long> list = new ArrayList<>();
        if (departamentos == null)
            return list;

        for (DepartamentoResponse dep : departamentos) {
            list.add(dep.getId());
        }
        return list;
    }

    private SearchFuncionarioResponse buscarFuncionarioByNombre(String pattern, int pageNmber) {
        return apiFuncionarioService.buscarFuncionarioByNombre(pattern, pageNmber);
    }

}
