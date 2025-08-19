package com.newsolicitudes.newsolicitudes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Postergacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface PostergacionRepository extends JpaRepository<Postergacion, Long> {

    Optional<Postergacion> findBySolicitud(Solicitud solicitud);

}
