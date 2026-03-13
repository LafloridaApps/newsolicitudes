package com.newsolicitudes.newsolicitudes.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.newsolicitudes.newsolicitudes.entities.Subrogancia;

public interface SubroganciaRepository extends JpaRepository<Subrogancia, Long>, JpaSpecificationExecutor<Subrogancia> {

        List<Subrogancia> findByIdDepto(Long idDepto);

        List<Subrogancia> findBySubrogante(Integer subrogante);

        List<Subrogancia> findBySubroganteAndFechaInicio(Integer subrogante, LocalDate fechaInicio);

        List<Subrogancia> findByJefeDepartamento(Integer jefe);

        Optional<Subrogancia> findFirstByJefeDepartamentoAndFechaInicioBetween(Integer subrogante, LocalDate desde,
                        LocalDate hasta);

        List<Subrogancia> findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Integer subrogante,
                        LocalDate fecha, LocalDate fecha2);

        List<Subrogancia> findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Integer jefe,
                        LocalDate fecha, LocalDate fecha2);

        boolean existsBySubroganteAndFechaInicioLessThanEqual(Integer subrogante, LocalDate fecha);

        boolean existsBySubroganteAndFechaInicioGreaterThanEqual(Integer subrogante, LocalDate fecha);

        boolean existsBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        Integer rut,
                        LocalDate fechaInicio,
                        LocalDate fechaTermino);

        Optional<Subrogancia> findFirstByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        Integer jefe, LocalDate fechaTermino, LocalDate fechaInicio);

        @Query(value = """
                        select * from subrogancia
                        where jefe_departamento =:jefeDepartamento  and id_depto=:depto
                        AND (fecha_inicio <= :fechaFin)
                          AND (fecha_fin >= :fechaInicio) """, nativeQuery = true)
        Optional<Subrogancia>  existeSubrogancia(Integer jefeDepartamento, LocalDate fechaInicio, LocalDate fechaFin, Long depto);

}
