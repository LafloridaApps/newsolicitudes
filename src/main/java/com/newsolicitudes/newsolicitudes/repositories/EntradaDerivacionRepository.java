package com.newsolicitudes.newsolicitudes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;

public interface EntradaDerivacionRepository extends JpaRepository<EntradaDerivacion,Long> {

    List<EntradaDerivacion> findByRut(Integer rut);

    Optional<EntradaDerivacion> findByDerivacionId(Long derivacionId);

    List<EntradaDerivacion> findByDerivacion_Solicitud_IdAndDerivacion_Solicitud_IdDepto(Long solicitudId, Long idDepartamento);

}
