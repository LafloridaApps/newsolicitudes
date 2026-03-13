package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class SearchViewFuncionarioResponse {

    private int currentPage;
    private int totalPages;
    private int totalItems;
    private Long size;
    List<SearchnNombreDto> funcionarios;

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return this.totalPages;

    }

    public void setTotalPage(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalItems() {
        return this.totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<SearchnNombreDto> getFuncionarios() {
        return this.funcionarios;
    }

    public void setFuncionarios(List<SearchnNombreDto> funcionarios) {
        this.funcionarios = funcionarios;
    }

}
