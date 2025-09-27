package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    Optional<Modulo> findByNombre(String nombre);
    
}
