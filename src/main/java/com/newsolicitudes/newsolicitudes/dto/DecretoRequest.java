package com.newsolicitudes.newsolicitudes.dto;

import java.util.Set;

public class DecretoRequest {

    private Set<Long> ids;
    private Integer rut;

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

}
