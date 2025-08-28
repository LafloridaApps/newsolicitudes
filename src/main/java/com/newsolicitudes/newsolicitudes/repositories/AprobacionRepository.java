package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface AprobacionRepository extends JpaRepository<Aprobacion, Long> {

    Optional<Aprobacion> findBySolicitud(Solicitud solicitud);

    Optional<Aprobacion> findBySolicitudAndFechaAprobacionBetween(Solicitud solicitud, LocalDate fechaInicio,
            LocalDate fechaFin);

    List<Aprobacion> findByFechaAprobacionBetween(LocalDate fechaInicio, LocalDate fechaFin);

}
