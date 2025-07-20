package com.newsolicitudes.newsolicitudes.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.FuncionarioPageResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioSearchResponse;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Departamento.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.exceptions.DepartamentoException;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.SearchFunc;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class SearcchFuncImpl implements SearchFunc {

    private final DepartamentoRepository departamentoRepository;

    private final FuncionarioRepository funcionarioRepository;

    public SearcchFuncImpl(DepartamentoRepository departamentoRepository, FuncionarioRepository funcionarioRepository) {
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public FuncionarioPageResponse searchFuncionario(String nombre, Long idDepartamento) {
        Departamento baseDepartamento = getDepartamentoById(idDepartamento);

        List<Long> ids = new ArrayList<>();
        ids.add(baseDepartamento.getId());

        collectChildDepartamentoIds(baseDepartamento, ids); // hijos
        collectUntilDireccion(baseDepartamento, ids); // padres

        Pageable pageable = PageRequest.of(0, 10, Sort.by("nombre").ascending());

        Page<Funcionario> pagesFuncionario = funcionarioRepository.findByDepartamentoIdInAndNombreContainingIgnoreCase(
                ids, nombre, pageable);

        List<FuncionarioSearchResponse> funcionarios = pagesFuncionario.getContent().stream().map(func -> {
            FuncionarioSearchResponse f = new FuncionarioSearchResponse();
            f.setNombre(func.getNombre());
            f.setRut(func.getRut());
            f.setVrut(func.getVrut().toString());
            f.setDepartamento(func.getNombreDepartamento());
            return f;
        }).toList();

        FuncionarioPageResponse response = new FuncionarioPageResponse();
        response.setFuncionarios(funcionarios);
        response.setTotalPages(pagesFuncionario.getTotalPages());
        response.setCurrentPage(pagesFuncionario.getNumber());
        response.setTotalElements(pagesFuncionario.getTotalElements());
        response.setPageSize(pagesFuncionario.getSize());

        return response;
    }

    private Departamento getDepartamentoById(Long idDepartamento) {
        return RepositoryUtils.findOrThrow(departamentoRepository.findById(idDepartamento),
                String.format("Departamento %d no encontrato", idDepartamento));
    }

    private void collectUntilDireccion(Departamento departamento, List<Long> result) {
        if (departamento == null)
            return;
        result.add(departamento.getId());
        if (departamento.getNivel() != NivelDepartamento.DIRECCION) {
            collectUntilDireccion(departamento.getDepartamentoSuperior(), result);
        }
    }

    private void collectChildDepartamentoIds(Departamento departamento, List<Long> result) {
        if (departamento == null)
            return;
        result.add(departamento.getId());
        for (Departamento child : departamento.getChildrens()) {
            collectChildDepartamentoIds(child, result);
        }
    }

    @Override
    public FuncionarioResponse getDirectorActivo(Long idDepartamento) {
        Departamento departamento = getDepartamentoById(idDepartamento);

        while (departamento != null && departamento.getNivel() != NivelDepartamento.DIRECCION) {
            departamento = departamento.getDepartamentoSuperior();
        }

        if (departamento == null) {
            throw new DepartamentoException("No se encontró un director activo en la jerarquía.");
        }

        return new FuncionarioResponse.Builder()
                .nombre(departamento.getNombreJefe())
                .rut(departamento.getRutJefe())
                .vrut(Character.toString(departamento.getVrutJefe()))
                .departamento(departamento.getNombreDepartamento())
                .codDepto(departamento.getId())
                .email(departamento.getEmailFuncionario()) // asegúrate de tener este método
                .nombreJefe(departamento.getNombreJefe())
                .build();
    }
}