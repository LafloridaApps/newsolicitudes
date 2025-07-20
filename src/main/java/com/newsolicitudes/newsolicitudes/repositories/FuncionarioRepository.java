package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByRut(Integer rut);

    Page<Funcionario> findByDepartamentoIdInAndNombreContainingIgnoreCase(List<Long> ids, String nombre, Pageable pageable);


}
