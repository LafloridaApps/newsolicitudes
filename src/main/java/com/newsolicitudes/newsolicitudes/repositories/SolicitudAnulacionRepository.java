package com.newsolicitudes.newsolicitudes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.SolicitudAnulacion;

public interface SolicitudAnulacionRepository extends JpaRepository<SolicitudAnulacion, Long>{

    boolean existsBySolicitudId(Long solicitudId);

    boolean existsBySolicitudIdAndEstado(Long solicitudId, SolicitudAnulacion.EstadoSolicitudAnulacion estado);

}
