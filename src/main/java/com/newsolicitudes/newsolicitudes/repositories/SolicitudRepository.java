package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.TipoSolicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findBySolicitanteAndFechaInicioAndTipoSolicitud(Funcionario funcionario, LocalDate fechaInicio, TipoSolicitud tipo );

}
