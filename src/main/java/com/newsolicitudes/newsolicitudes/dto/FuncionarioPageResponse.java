package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class FuncionarioPageResponse {

    private List<FuncionarioSearchResponse> funcionarios;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    public List<FuncionarioSearchResponse> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<FuncionarioSearchResponse> funcionarios) {
        this.funcionarios = funcionarios;
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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
