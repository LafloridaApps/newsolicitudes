package com.newsolicitudes.newsolicitudes.repositories;

import com.newsolicitudes.newsolicitudes.entities.Derivacion;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {
    @Query("SELECT d FROM Derivacion d LEFT JOIN EntradaDerivacion e ON d.id = e.derivacion.id WHERE d.idDepto IN :deptoIds AND e.id IS NULL")
    Page<Derivacion> findUnreadByIdDeptoIn(@Param("deptoIds") List<Long> deptoIds, Pageable pageable);

    List<Derivacion> findByIdDepto(Long idDepto);

    List<Derivacion> findBySolicitudId(Long solicitudId);

    Page<Derivacion> findByIdDepto(Long idDepto, Pageable pageable);

    Page<Derivacion> findByIdDeptoIn(List<Long> idDeptos, Pageable pageable);

    List<Derivacion> findByIdDeptoIn(List<Long> idDeptos);

    List<Derivacion> findBySolicitudIdOrderByFechaDerivacionDesc(Long solicitudId);

    List<Derivacion> findBySolicitudIdOrderByFechaDerivacionDescIdDesc(Long solicitudId);

    Optional<Derivacion> findTopBySolicitudIdAndIdDeptoOrderByFechaDerivacionDesc(Long solicitudId, Long idDepto);

    @Query("SELECT DISTINCT d.solicitud.id FROM Derivacion d WHERE d.idDepto IN :deptoIds AND d.solicitud.estado = com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud.PENDIENTE")
    List<Long> findSolicitudIdsByDeptoIdsAndEstadoPendiente(@Param("deptoIds") List<Long> deptoIds);

}
