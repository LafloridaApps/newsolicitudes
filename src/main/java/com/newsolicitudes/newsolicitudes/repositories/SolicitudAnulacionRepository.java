package com.newsolicitudes.newsolicitudes.repositories;

import com.newsolicitudes.newsolicitudes.entities.SolicitudAnulacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitudAnulacionRepository extends JpaRepository<SolicitudAnulacion, Long> {

    // Método que ya estabas utilizando (o que también debes agregar si marca error en la línea 61)
    boolean existsBySolicitudIdAndEstado(Long solicitudId, SolicitudAnulacion.EstadoSolicitudAnulacion estado);

    // Agrega esta nueva línea para solucionar el error actual:
    Optional<SolicitudAnulacion> findBySolicitudIdAndEstado(Long solicitudId, SolicitudAnulacion.EstadoSolicitudAnulacion estado);

}
