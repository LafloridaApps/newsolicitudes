package com.newsolicitudes.newsolicitudes.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoRequest;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.JefeFunc;
import com.newsolicitudes.newsolicitudes.entities.CodigoExterno;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Departamento.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.repositories.CodigoExternoRepository;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private FuncionarioRepository funcionarioRepository;

    private final DepartamentoRepository departamentoRepository;

    private final CodigoExternoRepository codigoExternoRepository;

    public DepartamentoServiceImpl(DepartamentoRepository departamentoRepository,
            FuncionarioRepository funcionarioRepository, CodigoExternoRepository codigoExternoRepository) {
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.codigoExternoRepository = codigoExternoRepository;
    }

    @Override
    public DepartamentoResponse createDepartamento(DepartamentoRequest request) {

        Departamento departamento = new Departamento();
        departamento.setNombreDepartamento(request.getNombre());
        departamento.setNivel(Departamento.NivelDepartamento.valueOf(request.getNivel().toUpperCase()));

        Funcionario jefeDepartamento = getFuncionarioByRut(request.getRutJefe());

        departamento.setJefe(jefeDepartamento);

        if (request.getPadreId() != null) {
            Departamento padre = getDepartamentoById(request.getPadreId());
            departamento.setDepartamentoSuperior(padre);
        }

        departamentoRepository.save(departamento);
        return new DepartamentoResponse(departamento.getId(), departamento.getNombreDepartamento());
    }

    @Override
    public List<DepartamentoList> getDepartamentosList() {
        List<Departamento> raices = departamentoRepository.findAll()
                .stream()
                .filter(d -> d.getNivel().toString().equalsIgnoreCase("ALCALDIA"))
                .toList();

        return raices.stream()
                .map(this::mapDto)
                .toList();
    }

    private DepartamentoList mapDto(Departamento departamento) {
        DepartamentoList dto = new DepartamentoList();
        dto.setId(departamento.getId());
        dto.setNombre(departamento.getNombreDepartamento());
        dto.setNivel(departamento.getNivel().toString());
        dto.setRutJefe(departamento.getRutJefe());
        dto.setNombreJefe(departamento.getNombreJefe());
        dto.setVrutJefe(departamento.getVrutJefe() != null ? departamento.getVrutJefe().toString() : null);
        dto.setEmail(departamento.getEmailFuncionario() != null ? departamento.getEmailFuncionario() : null);

        List<Departamento> hijos = departamento.getChildrens();
        if (hijos != null && !hijos.isEmpty()) {
            dto.setDependencias(
                    hijos.stream()
                            .map(this::mapDto)
                            .toList());
        }

        return dto;
    }

    @Override
    public void updateDepartamento(Long idDepartamento, DepartamentoRequest request) {

        Departamento departamento = getDepartamentoById(idDepartamento);

        departamento.setNombreDepartamento(request.getNombre());

        departamentoRepository.save(departamento);

    }

    private Departamento getDepartamentoById(Long id) {
        return RepositoryUtils.findOrThrow(departamentoRepository.findById(id),
                String.format("Departamento %d no encotrado", id));

    }

    private Funcionario getFuncionarioByRut(Integer rut) {
        return RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                String.format("Funcionario %d no encontrado", rut));

    }

    private CodigoExterno getCodigoExternoByCodEx(String codEx) {
        return RepositoryUtils.findOrThrow(codigoExternoRepository.findByCodigoEx(codEx),
                String.format("Codigo %s no existe", codEx));
    }

    @Override
    public void updateJefeDeparatmento(Long idDepto, Integer rut) {
        Funcionario funcionario = getFuncionarioByRut(rut);

        Departamento departamento = getDepartamentoById(idDepto);

        departamento.setJefe(funcionario);
        departamentoRepository.save(departamento);
    }

    @Override
    public JefeFunc isJefeDepartamento(String codEx, Integer rut) {
        CodigoExterno deptoExterno = getCodigoExternoByCodEx(codEx);

        Funcionario funcionario = getFuncionarioByRut(rut);

        Optional<Departamento> optDepto = departamentoRepository.findByIdAndJefe(deptoExterno.getIdDepto(),
                funcionario);

        Departamento depto = RepositoryUtils.findOrThrow(departamentoRepository.findById(deptoExterno.getIdDepto()),
                String.format("El funcionario %d no es jefe del departamento %d", rut, deptoExterno.getIdDepto()));

        return new JefeFunc(optDepto.isPresent(), (depto.getNivel().equals(NivelDepartamento.DIRECCION)
                || depto.getNivel().equals(NivelDepartamento.ADMINISTRACION)));

    }

}
