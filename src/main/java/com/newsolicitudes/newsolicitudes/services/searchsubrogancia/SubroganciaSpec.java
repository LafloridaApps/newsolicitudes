package com.newsolicitudes.newsolicitudes.services.searchsubrogancia;

import org.springframework.data.jpa.domain.Specification;

import com.newsolicitudes.newsolicitudes.entities.Subrogancia;

public class SubroganciaSpec {

    private SubroganciaSpec() {

    }

    public static Specification<Subrogancia> hasRutJefe(Integer jefe) {

        return (root, query, cb) -> {
            if (jefe == null)
                return cb.conjunction();
            return cb.equal(root.get("jefeDepartamento"), jefe);

        };
    }

    public static Specification<Subrogancia> hasSubrogante(Integer subrogante) {

        return (root, query, cb) -> {
            if (subrogante == null)
                return cb.conjunction();
            return cb.equal(root.get("subrogante"), subrogante);

        };
    }

    public static Specification<Subrogancia> hasDepartamento(Long idDepto) {

        return (root, query, cb) -> {
            if (idDepto == null)
                return cb.conjunction();
            return cb.equal(root.get("idDepto"), idDepto);

        };
    }

}
