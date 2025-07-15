package com.newsolicitudes.newsolicitudes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

}
