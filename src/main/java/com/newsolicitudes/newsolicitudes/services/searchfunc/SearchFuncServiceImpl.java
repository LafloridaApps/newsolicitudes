package com.newsolicitudes.newsolicitudes.services.searchfunc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest; // Importar PageRequest
import org.springframework.data.domain.Pageable; // Importar Pageable

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.SearchFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.AusenciaException;
import com.newsolicitudes.newsolicitudes.exceptions.DepartamentoException;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.apiausencias.ApiAusenciasService;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.apifuncionario.ApiExtFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.decretos.DecretoService; // Importar DecretoService
import com.newsolicitudes.newsolicitudes.dto.DecretoDto; // Importar DecretoDto

@Service
public class SearchFuncServiceImpl implements SearchFuncServcie {

    private final ApiDepartamentoService apiDepartamentoService;
    private final ApiAusenciasService apiAusenciasService;
    private final ApiExtFuncionarioService apiFuncionarioService;
    private final SubroganciaRepository subroganciaRepository;
    private final DecretoService decretoService; // Inyectar DecretoService

    public SearchFuncServiceImpl(ApiDepartamentoService apiDepartamentoService,
            ApiAusenciasService apiAusenciasService, ApiExtFuncionarioService apiFuncionarioService,
            SubroganciaRepository subroganciaRepository,
            DecretoService decretoService) { // Modificar constructor
        this.apiDepartamentoService = apiDepartamentoService;
        this.apiAusenciasService = apiAusenciasService;
        this.apiFuncionarioService = apiFuncionarioService;
        this.subroganciaRepository = subroganciaRepository;
        this.decretoService = decretoService; // Asignar DecretoService
    }

    @Override
    public FuncionarioResponseApi getDirectorActivo(Long idDepartamento, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud) {

        DepartamentoResponse departamento = findDireccion(idDepartamento);

        if (departamento == null) {
            throw new DepartamentoException(
                    "No se pudo encontrar una dirección para el departamento con ID: " + idDepartamento);
        }

        FuncionarioResponseApi director = buscarFuncionarioByRut(departamento.getRutJefe());

        if (!hasAusenciasBetweenDates(director, fechaInicioSolicitud, fechaFinSolicitud)) {
            director = findSubrogante(director, fechaInicioSolicitud, fechaFinSolicitud);
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

    private FuncionarioResponseApi findSubrogante(FuncionarioResponseApi director, LocalDate fechaInicioSolicitud,
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

    private boolean hasAusenciasBetweenDates(FuncionarioResponseApi funcionario,
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

    private FuncionarioResponseApi buscarFuncionarioByRut(Integer rut) {
        return apiFuncionarioService.obtenerDetalleColaborador(rut);
    }

    @Override
    public FuncionarioResponseApi buscarSubroganteByRut(Integer rut, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud) {

        FuncionarioResponseApi funcionario = buscarFuncionarioByRut(rut);

        if (hasAusenciasBetweenDates(funcionario, fechaInicioSolicitud, fechaFinSolicitud)) {
            throw new AusenciaException(
                    "El funcionario con RUT: " + rut + " tiene ausencias en las fechas solicitadas");
        }

        return funcionario;

    }

    @Override
    public List<FuncionarioResponseApi> buscarFuncionarioByNombre(String pattern, LocalDate fechaInicioSolicitud,
            LocalDate fechaFinSolicitud, int pageNmber, Long iddepto) {

        SearchFuncionarioResponse searchFuncionarioResponse = buscarFuncionarioByNombre(pattern, pageNmber);

        List<FuncionarioResponseApi> funcionarios = searchFuncionarioResponse.getFuncionarios().stream()
                .map(f -> buscarFuncionarioByRut(f.getRut()))
                .filter(a -> !hasAusenciasBetweenDates(a, fechaInicioSolicitud, fechaFinSolicitud))
                .toList();

        // Buscar y asignar decretos a cada funcionario
        for (FuncionarioResponseApi funcionario : funcionarios) {
            Pageable pageable = PageRequest.of(0, 100); // Puedes ajustar el tamaño de página si es necesario
            List<DecretoDto> decretos = decretoService.searchDecretos(null, null, null, funcionario.getRut(), null, null, pageable)
                                                    .getContent()
                                                    .stream()
                                                    .map(dto -> new DecretoDto(dto.getIdDecreto(), dto.getFechaDecreto())) // Asumiendo que DecretoConSolicitudesDTO tiene un getDecreto() que devuelve DecretoDto
                                                    .toList();
            funcionario.setDecretos(decretos);
        }

        return funcionarios;

    }

    

    private SearchFuncionarioResponse buscarFuncionarioByNombre(String pattern, int pageNmber) {
        return apiFuncionarioService.buscarFuncionarioByNombre(pattern, pageNmber);
    }

}