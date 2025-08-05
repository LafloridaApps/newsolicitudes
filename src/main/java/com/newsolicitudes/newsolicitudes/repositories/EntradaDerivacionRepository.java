package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;

public interface EntradaDerivacionRepository extends JpaRepository<EntradaDerivacion,Long> {

    List<EntradaDerivacion> findByRut(Integer rut);

}
