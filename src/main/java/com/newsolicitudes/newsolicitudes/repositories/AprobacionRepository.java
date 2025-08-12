package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface AprobacionRepository extends JpaRepository<Aprobacion,Long> {

    Optional<Aprobacion> findBySolicitud(Solicitud solicitud);

}
