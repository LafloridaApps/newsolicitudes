package com.newsolicitudes.newsolicitudes.repositories;

import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {
    @Query("SELECT d FROM Derivacion d LEFT JOIN EntradaDerivacion e ON d.id = e.derivacion.id WHERE d.idDepto IN :deptoIds AND e.id IS NULL AND (:year IS NULL OR YEAR(d.solicitud.fechaSolicitud) = :year) ORDER BY d.solicitud.id DESC")
    Page<Derivacion> findUnreadByIdDeptoIn(@Param("deptoIds") List<Long> deptoIds, @Param("year") Integer year, Pageable pageable);

    List<Derivacion> findByIdDepto(Long idDepto);

    List<Derivacion> findBySolicitudId(Long solicitudId);

    Page<Derivacion> findByIdDepto(Long idDepto, Pageable pageable);

    @Query("SELECT d FROM Derivacion d WHERE d.idDepto IN :idDeptos AND (:year IS NULL OR YEAR(d.solicitud.fechaSolicitud) = :year) ORDER BY d.solicitud.id DESC")
    Page<Derivacion> findByIdDeptoIn(@Param("idDeptos") List<Long> idDeptos, @Param("year") Integer year, Pageable pageable);

    List<Derivacion> findByIdDeptoIn(List<Long> idDeptos);

    List<Derivacion> findBySolicitudIdOrderByFechaDerivacionDesc(Long solicitudId);

    List<Derivacion> findBySolicitudIdOrderByFechaDerivacionDescIdDesc(Long solicitudId);

    Optional<Derivacion> findTopBySolicitudIdAndIdDeptoOrderByFechaDerivacionDesc(Long solicitudId, Long idDepto);

    @Query("SELECT DISTINCT d.solicitud.id FROM Derivacion d WHERE d.idDepto IN :deptoIds AND d.solicitud.estado = com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud.PENDIENTE AND d.estadoDerivacion = com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion.PENDIENTE")
    List<Long> findSolicitudIdsByDeptoIdsAndEstadoPendiente(@Param("deptoIds") List<Long> deptoIds);

    List<Derivacion> findBySolicitud(Solicitud solicitud);



}
