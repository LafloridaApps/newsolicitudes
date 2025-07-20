package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.ApiFuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.CodigoExterno;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.exceptions.FuncionarioException;
import com.newsolicitudes.newsolicitudes.repositories.CodigoExternoRepository;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;
import com.newsolicitudes.newsolicitudes.utlils.PersonaUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    private final ApiFuncionarioService apiFuncionarioService;

    private final DepartamentoRepository departamentoRepository;

    private final CodigoExternoRepository codigoExternoRepository;

    public FuncionarioServiceImpl(FuncionarioRepository funcionarioRepository,
            ApiFuncionarioService apiFuncionarioService,
            DepartamentoRepository departamentoRepository, CodigoExternoRepository codigoExternoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.apiFuncionarioService = apiFuncionarioService;
        this.departamentoRepository = departamentoRepository;
        this.codigoExternoRepository = codigoExternoRepository;
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
    public FuncionarioResponse getFuncionarioInfo(Integer rut, String vRut) {

        boolean rutValido = PersonaUtils.validateRut(rut, vRut);

        if (!rutValido) {
            throw new FuncionarioException("Rut inválido");
        }

        

        ApiFuncionarioResponse response = apiFuncionarioService.obtenerDetalleColaborador(rut);

        if (response == null) {
            throw new FuncionarioException("No se encontro el funcionario en la base de datos municipalidad");
        }

        CodigoExterno deptoExt = getByCodigoEx(response.getCodDeptoExt());
        Departamento departamento = getDepartamentoById(deptoExt.getIdDepto());

        Funcionario funcionario = funcionarioRepository.findByRut(rut).orElseGet(() -> createFuncionario(response));

        FuncionarioResponse funcionarioResponse = getFuncionarioByRut(funcionario.getRut());
        funcionarioResponse.setFoto(response.getFoto());
        funcionarioResponse.setIdent(response.getIdent());

        if (funcionario.getDepartamento() == null
                || !funcionario.getIdDepto().equals(departamento.getId())) {
            funcionario.setDepartamento(departamento);
            funcionarioRepository.save(funcionario);
        }

        Funcionario jefeDirecto = buscarJefeEnJerarquia(departamento);

        if (jefeDirecto != null) {
            response.setJefe(jefeDirecto.getNombre());
            funcionarioResponse.setNombreJefe(jefeDirecto.getNombre());
            funcionarioResponse.setCodDeptoJefe(jefeDirecto.getIdDepto());
        }

        if (jefeDirecto != null && jefeDirecto.getRut().equals(rut)) {
            Departamento superior = departamento.getDepartamentoSuperior();
            Funcionario jefeSuperior = buscarJefeEnJerarquia(superior);

            if (jefeSuperior != null) {
                response.setJefe(jefeSuperior.getNombre());
                funcionarioResponse.setNombreJefe(jefeSuperior.getNombre());
                funcionarioResponse.setCodDeptoJefe(jefeSuperior.getIdDepto());
            }
        }

        return funcionarioResponse;

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

    
    public FuncionarioResponse getFuncionarioByRut(Integer rut) {

        Funcionario funcionario = RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                String.format("No se encontro el funcionario con el rut %d", rut));

        return new FuncionarioResponse.Builder()
                .nombre(funcionario.getNombre())
                .rut(funcionario.getRut())
                .vrut(Character.toString(funcionario.getVrut()))
                .departamento(funcionario.getNombreDepartamento())
                .codDepto(funcionario.getIdDepto())
                .departamento(funcionario.getNombreDepartamento())
                .email(funcionario.getEmail())
                .build();

    }

    private Departamento getDepartamentoById(Long id) {
        return RepositoryUtils.findOrThrow(departamentoRepository.findById(id),
                String.format("No existe el departamento %d", id));
    }

    private CodigoExterno getByCodigoEx(String codEx) {
        return RepositoryUtils.findOrThrow(codigoExternoRepository.findByCodigoEx(codEx),
                String.format("No se encontró el departamento externo %s", codEx));

    }

}
