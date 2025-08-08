package com.newsolicitudes.newsolicitudes.repositories;


import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;

public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {
    List<Derivacion> findByIdDeptoAndEstadoDerivacion(Long idDepto, EstadoDerivacion estadoDerivacion);

}
