package com.newsolicitudes.newsolicitudes.services.searchsubrogancia;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.SearchSubroganciResponse;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

@Component
public class SearchMapper {

    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;

    public SearchMapper(FuncionarioService funcionarioService, DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
    }

    public List<SearchSubroganciResponse> mapToDto(List<Subrogancia> subrogancias) {

        return subrogancias.stream().map(s -> {

            FuncionarioResponseApi jefeDepartamento = getFuncionario(s.getJefeDepartamento());
            FuncionarioResponseApi subrogante = getFuncionario(s.getSubrogante());
            DepartamentoResponse depto = getDepartamento(s.getIdDepto());
            Character vrutJefe = jefeDepartamento.getVrut().charAt(0);
            Character vrutSubrogante = subrogante.getVrut().charAt(0);

            return new SearchSubroganciResponse.Builder()
                    .nombreJefe(jefeDepartamento.getNombreCompleto())
                    .subrogante(subrogante.getNombreCompleto())
                    .departamento(depto.getNombre())
                    .fechaInicio(s.getFechaInicio())
                    .fechaFin(s.getFechaFin())
                    .estado(estado(s.getFechaFin()))
                    .rutJefe(jefeDepartamento.getRut())
                    .vrutJefe(vrutJefe)
                    .rutSobrogante(subrogante.getRut())
                    .vrutSubrogante(vrutSubrogante)
                    .idSubrogancia(s.getId())
                    .build();

        }

        ).sorted(Comparator.comparing(SearchSubroganciResponse::getFechaFin).reversed())
                .toList();

    }

    private FuncionarioResponseApi getFuncionario(Integer rut) {
        return funcionarioService.getFuncionarioByRut(rut);
    }

    private DepartamentoResponse getDepartamento(Long idDepto) {
        return departamentoService.getDepartamentoById(idDepto);
    }

    private String estado(LocalDate fechaFin) {
        return LocalDate.now().isAfter(fechaFin) ? "FINALIZADA" : "ACTIVA";
    }

}
