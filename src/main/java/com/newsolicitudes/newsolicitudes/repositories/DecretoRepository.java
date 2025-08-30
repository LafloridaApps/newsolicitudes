package com.newsolicitudes.newsolicitudes.repositories;

import com.newsolicitudes.newsolicitudes.entities.Decreto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DecretoRepository extends JpaRepository<Decreto, Long> {
    List<Decreto> findAllByFechaDecretoBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
