package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class PageSolicitudesResponse {

    private int currentPage;
    private int totalPages;
    private int pageSize;
    private Long totalElements;

    private List<SolicitudDto> solicitudes;

    public PageSolicitudesResponse() {
    }

    public PageSolicitudesResponse(int currentPage, int totalPages, int pageSize, Long totalElements,
            List<SolicitudDto> solicitudes) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.solicitudes = solicitudes;
    }

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

    public List<SolicitudDto> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<SolicitudDto> solicitudes) {
        this.solicitudes = solicitudes;
    }

}
