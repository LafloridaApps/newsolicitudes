package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.TipoSolicitud;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByRutAndFechaInicioAndTipoSolicitud(Integer rut, LocalDate fechaInicio, TipoSolicitud tipo);

    Page<Solicitud> findByRut(Integer rut, Pageable pageable);

    Optional<List<Solicitud>> findByRutAndFechaInicioBetween(Integer rut, LocalDate fechaInicio, LocalDate fechaFin);

    Optional<Solicitud> findTopByRutOrderByFechaSolicitudDesc(Integer rut);

    List<Solicitud> findByEstadoAndFechaInicioBetween(Solicitud.EstadoSolicitud estado, LocalDate fechaInicio,
            LocalDate fechaFin);

    List<Solicitud> findByEstadoAndFechaTerminoBetween(Solicitud.EstadoSolicitud estado, LocalDate fechaInicio,
            LocalDate fechaFin);

    List<Solicitud> findByEstadoAndIdDeptoAndFechaInicioBetween(Solicitud.EstadoSolicitud estado, Long idDepto,
            LocalDate fechaInicio, LocalDate fechaFin);

    List<Solicitud> findByEstadoAndIdDeptoAndFechaTerminoBetween(Solicitud.EstadoSolicitud estado, Long idDepto,
            LocalDate fechaInicio, LocalDate fechaFin);

    List<Solicitud> findByEstadoAndIdDeptoAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
            Solicitud.EstadoSolicitud estado, Long idDepto, LocalDate fechaInicio, LocalDate fechaFin);

    List<Solicitud> findByEstadoInAndIdDeptoAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
            List<Solicitud.EstadoSolicitud> estados, Long idDepto, LocalDate fechaInicio, LocalDate fechaFin);

    List<Solicitud> findByEstado(Solicitud.EstadoSolicitud estado);

    List<Solicitud> findAllByEstadoAndIdDeptoIn(Solicitud.EstadoSolicitud estado, List<Long> idDeptos);

    List<Solicitud> findByEstadoInAndIdDeptoInAndFechaInicioGreaterThanEqual(List<Solicitud.EstadoSolicitud> estados,
            List<Long> idDeptos,
            LocalDate fechaInicio);

}
