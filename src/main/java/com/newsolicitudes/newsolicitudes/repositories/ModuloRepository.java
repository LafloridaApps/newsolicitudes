package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    Page<Modulo> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Optional<Modulo> findByNombre(String nombre);
    
}
