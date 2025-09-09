
package com.newsolicitudes.newsolicitudes.services.decretos;

import com.newsolicitudes.newsolicitudes.entities.Decreto;
import com.newsolicitudes.newsolicitudes.entities.DecretoSolicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class DecretoSpecification {

    private DecretoSpecification() {
        throw new IllegalStateException("Utility class");
    }

    private static final String FECHA_DECRETO = "fechaDecreto";
    private static final String ID = "id";
    private static final String RUT = "rut";
    private static final String DECRETO_SOLICITUDES = "decretoSolicitudes";
    private static final String SOLICITUD = "solicitud";

    public static Specification<Decreto> withFechaDecretoBetween(LocalDate fechaDesde, LocalDate fechaHasta) {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FECHA_DECRETO), fechaDesde));
            }

            if (fechaHasta != null) {
                // Use lessThan the next day to include the entire 'fechaHasta' day.
                // This is robust against columns with a time component (DATETIME/TIMESTAMP).
                predicates.add(criteriaBuilder.lessThan(root.get(FECHA_DECRETO), fechaHasta.plusDays(1)));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    public static Specification<Decreto> withDecretoId(Long decretoId) {
        return (root, query, criteriaBuilder) -> {
            if (decretoId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get(ID), decretoId);
        };
    }

    public static Specification<Decreto> withRutFuncionario(Integer rut) {
        return (root, query, criteriaBuilder) -> {
            if (rut == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Decreto, DecretoSolicitud> decretoSolicitudJoin = root.join(DECRETO_SOLICITUDES);
            Join<DecretoSolicitud, Solicitud> solicitudJoin = decretoSolicitudJoin.join(SOLICITUD);
            return criteriaBuilder.equal(solicitudJoin.get(RUT), rut);
        };
    }

    public static Specification<Decreto> withSolicitudId(Long solicitudId) {
        return (root, query, criteriaBuilder) -> {
            if (solicitudId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Decreto, DecretoSolicitud> decretoSolicitudJoin = root.join(DECRETO_SOLICITUDES);
            Join<DecretoSolicitud, Solicitud> solicitudJoin = decretoSolicitudJoin.join(SOLICITUD);
            return criteriaBuilder.equal(solicitudJoin.get(ID), solicitudId);
        };
    }

    public static Specification<Decreto> byFuncionarioRutsIn(java.util.List<Integer> ruts) {
        return (root, query, criteriaBuilder) -> {
            if (ruts == null || ruts.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filter if list is empty
            }
            Join<Decreto, DecretoSolicitud> decretoSolicitudJoin = root.join(DECRETO_SOLICITUDES);
            Join<DecretoSolicitud, Solicitud> solicitudJoin = decretoSolicitudJoin.join(SOLICITUD);
            return solicitudJoin.get(RUT).in(ruts);
        };
    }

}
