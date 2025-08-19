package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.TipoSolicitud;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByRutAndFechaInicioAndTipoSolicitud(Integer rut, LocalDate fechaInicio, TipoSolicitud tipo );

    Page<Solicitud> findByRut(Integer rut, Pageable pageable);

    Optional<Solicitud> findByRutAndFechaInicioBetween(Integer rut, LocalDate fechaInicio, LocalDate fechaFin);

}
