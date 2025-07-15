package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;



public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {

    List<Derivacion> findByDepartamentoIn(List<Departamento> departamentos);


}
