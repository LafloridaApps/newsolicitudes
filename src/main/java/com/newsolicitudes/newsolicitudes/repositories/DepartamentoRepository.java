package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    List<Departamento> findByJefe(Funcionario jefe);

    List<Departamento> findByFuncionarios(Funcionario funcionario);

    Optional<Departamento> findByIdAndJefe(Long id, Funcionario funcionario);

    Optional<Departamento> findByNombreDepartamentoLike(String nombreDepartamento);

}
