package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class PageMiSolicitudResponse {

    private int currentPage;
    private int totalPages;
    private int pageSize;
    private Long totalElements;
    List<MiSolicitudDto> solicitudes;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public List<MiSolicitudDto> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<MiSolicitudDto> solicitudes) {
        this.solicitudes = solicitudes;
    }

}
