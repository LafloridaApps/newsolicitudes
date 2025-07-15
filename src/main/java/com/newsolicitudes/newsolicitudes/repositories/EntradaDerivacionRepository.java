package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;

public interface EntradaDerivacionRepository extends JpaRepository<EntradaDerivacion,Long> {

    List<EntradaDerivacion> findByFuncionario(Funcionario funcionario);

}
