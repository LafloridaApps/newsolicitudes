package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class DashboardResponseDto {

    private KpisDto kpis;
    private List<MesMetricaDto> porMes;
    private List<DepartamentoMetricaDto> porDepartamento;
    private List<DepartamentoDropdownDto> departamentosDropdown;

    public KpisDto getKpis() {
        return kpis;
    }

    public void setKpis(KpisDto kpis) {
        this.kpis = kpis;
    }

    public List<MesMetricaDto> getPorMes() {
        return porMes;
    }

    public void setPorMes(List<MesMetricaDto> porMes) {
        this.porMes = porMes;
    }

    public List<DepartamentoMetricaDto> getPorDepartamento() {
        return porDepartamento;
    }

    public void setPorDepartamento(List<DepartamentoMetricaDto> porDepartamento) {
        this.porDepartamento = porDepartamento;
    }

    public List<DepartamentoDropdownDto> getDepartamentosDropdown() {
        return departamentosDropdown;
    }

    public void setDepartamentosDropdown(List<DepartamentoDropdownDto> departamentosDropdown) {
        this.departamentosDropdown = departamentosDropdown;
    }

}