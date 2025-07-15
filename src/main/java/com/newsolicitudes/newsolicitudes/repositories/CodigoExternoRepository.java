package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.CodigoExterno;

public interface CodigoExternoRepository extends JpaRepository<CodigoExterno, Long> {

    Optional<CodigoExterno> findByCodigoEx(String codigoEx);

}
