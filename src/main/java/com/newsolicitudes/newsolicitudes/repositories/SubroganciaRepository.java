package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsolicitudes.newsolicitudes.entities.Subrogancia;

public interface SubroganciaRepository extends JpaRepository<Subrogancia, Long> {

    List<Subrogancia> findByIdDepto(Long idDepto);

    List<Subrogancia> findBySubrogante(Integer subrogante);

    List<Subrogancia> findBySubroganteAndFechaInicio(Integer subrogante, LocalDate fechaInicio);

    List<Subrogancia> findByJefeDepartamento(Integer jefe);

    Optional<Subrogancia> findFirstByJefeDepartamentoAndFechaInicioBetween(Integer subrogante, LocalDate desde,
            LocalDate hasta);

    List<Subrogancia> findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Integer subrogante, LocalDate fecha, LocalDate fecha2);

    List<Subrogancia> findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Integer jefe, LocalDate fecha, LocalDate fecha2);

}
