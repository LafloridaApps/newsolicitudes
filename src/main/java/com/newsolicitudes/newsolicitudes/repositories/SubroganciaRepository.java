package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;

public interface SubroganciaRepository extends JpaRepository<Subrogancia, Long> {

    List<Subrogancia> findByDepartamento(Departamento departamento);

    List<Subrogancia> findBySubrogante(Funcionario subrogante);

    List<Subrogancia> findByJefeDepartamento(Funcionario jefe);

Optional<Subrogancia> findFirstByJefeDepartamentoAndFechaInicioBetween(Funcionario subrogante, LocalDate desde, LocalDate hasta);


}
