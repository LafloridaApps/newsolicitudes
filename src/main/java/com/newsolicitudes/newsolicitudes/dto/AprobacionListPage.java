package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class AprobacionListPage {

    private int totalPages;
    private long totalElements;
    private int currentPage;
    private List<AprobacionList> aprobaciones;

    public AprobacionListPage(int totalPages, long totalElements, int currentPage, List<AprobacionList> aprobaciones) {
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.aprobaciones = aprobaciones;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<AprobacionList> getAprobaciones() {
        return aprobaciones;
    }

    public void setAprobaciones(List<AprobacionList> aprobaciones) {
        this.aprobaciones = aprobaciones;
    }

}
