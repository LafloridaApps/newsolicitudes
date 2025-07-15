package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;

public interface SubroganciaRepository extends JpaRepository<Subrogancia, Long> {

    List<Subrogancia> findByDepartamento(Departamento departamento);

    List<Subrogancia> findBySubrogante(Funcionario jefe);

}
