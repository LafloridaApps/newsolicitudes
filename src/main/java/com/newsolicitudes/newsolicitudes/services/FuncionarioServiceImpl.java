package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    private final ApiFuncionarioService apiFuncionarioService;

    private final DepartamentoRepository departamentoRepository;

    public FuncionarioServiceImpl(FuncionarioRepository funcionarioRepository,
            ApiFuncionarioService apiFuncionarioService,
            DepartamentoRepository departamentoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.apiFuncionarioService = apiFuncionarioService;
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    public Funcionario getOrCreateFuncionario(ApiFuncionarioResponse request) {

        return funcionarioRepository.findByRut(request.getRut())
                .orElseGet(() -> createFuncionario(request));

    }

    private Funcionario createFuncionario(ApiFuncionarioResponse request) {

        Funcionario funcionario = new Funcionario();

        funcionario.setRut(request.getRut());
        funcionario.setNombre(request.getNombres().concat(" ").concat(request.getPaterno()));
        funcionario.setEmail(request.getEmail());

        return funcionarioRepository.save(funcionario);

    }

    @Override
    public ApiFuncionarioResponse getFuncionarioInfo(Integer rut) {

        ApiFuncionarioResponse response = apiFuncionarioService.obtenerDetalleColaborador(rut);

        Departamento departamento = getDepartamentoByNombre(response.getDepartamento());

        Funcionario funcionario = funcionarioRepository.findByRut(rut).orElseGet(() -> createFuncionario(response));

        if (funcionario.getDepartamento() == null) {
            funcionario.setDepartamento(departamento);
            funcionarioRepository.save(funcionario);
        }

        Funcionario jefeDirecto = buscarJefeEnJerarquia(departamento);

        if (jefeDirecto != null) {
            response.setJefe(jefeDirecto.getNombre());
        }

        if (jefeDirecto != null && jefeDirecto.getRut().equals(rut)) {
            Departamento superior = departamento.getDepartamentoSuperior();
            Funcionario jefeSuperior = buscarJefeEnJerarquia(superior);

            if (jefeSuperior != null) {
                response.setJefe(jefeSuperior.getNombre());
            }
        }

        return response;

    }

    private Funcionario buscarJefeEnJerarquia(Departamento departamento) {
        while (departamento != null) {
            Funcionario jefe = departamento.getJefe();
            if (jefe != null) {
                return jefe;
            }
            departamento = departamento.getDepartamentoSuperior();
        }
        return null;
    }

    @Override
    public Funcionario getFuncionarioByRut(Integer rut) {

        return RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                String.format("No se encontro el funcionario con el rut %d", rut));

    }

    private Departamento getDepartamentoByNombre(String nombreDepartamento) {
        return RepositoryUtils.findOrThrow(departamentoRepository.findByNombreDepartamentoLike(nombreDepartamento),
                String.format("No existe el departamento %s", nombreDepartamento));
    }

}
