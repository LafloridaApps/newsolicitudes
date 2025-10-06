package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

public interface AprobacionRepository extends JpaRepository<Aprobacion, Long> {

    Optional<Aprobacion> findBySolicitud(Solicitud solicitud);

    Optional<Aprobacion> findBySolicitudAndFechaAprobacionBetween(Solicitud solicitud, LocalDate fechaInicio,
            LocalDate fechaFin);

    List<Aprobacion> findByFechaAprobacionBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT a FROM Aprobacion a WHERE a.fechaAprobacion BETWEEN :fechaInicio AND :fechaFin")
    Page<Aprobacion> findByFechaAprobacionBetween(@Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin, Pageable pageable);

}
