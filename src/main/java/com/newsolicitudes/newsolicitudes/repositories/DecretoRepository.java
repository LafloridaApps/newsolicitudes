package com.newsolicitudes.newsolicitudes.repositories;

import com.newsolicitudes.newsolicitudes.entities.Decreto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DecretoRepository extends JpaRepository<Decreto, Long>, JpaSpecificationExecutor<Decreto> {
    List<Decreto> findAllByFechaDecretoBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
