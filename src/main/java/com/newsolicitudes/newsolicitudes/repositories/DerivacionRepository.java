package com.newsolicitudes.newsolicitudes.repositories;

import com.newsolicitudes.newsolicitudes.entities.Derivacion;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Pageable;

public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {
    List<Derivacion> findByIdDepto(Long idDepto);

    List<Derivacion> findBySolicitudId(Long solicitudId);

    Page<Derivacion> findByIdDepto(Long idDepto, Pageable pageable);

    Page<Derivacion> findByIdDeptoIn(List<Long> idDeptos, Pageable pageable);

    List<Derivacion> findBySolicitudIdOrderByFechaDerivacionDesc(Long solicitudId);

}
