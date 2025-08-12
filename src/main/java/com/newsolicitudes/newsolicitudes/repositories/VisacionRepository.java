package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Visacion;

public interface VisacionRepository extends JpaRepository<Visacion, Long> {

    boolean existsBySolicitud(Solicitud solicitud);

    Optional<Visacion> findBySolicitud(Solicitud solicitud);

}
