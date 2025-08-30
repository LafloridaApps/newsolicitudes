package com.newsolicitudes.newsolicitudes.dto;

import java.util.Set;

public class DecretoDeleteRequest {
    private Set<Long> ids;

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }
}