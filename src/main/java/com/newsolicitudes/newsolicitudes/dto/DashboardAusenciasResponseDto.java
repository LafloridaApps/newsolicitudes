package com.newsolicitudes.newsolicitudes.dto;

import java.util.List;

public class DashboardAusenciasResponseDto {

    private List<DashboardAusenciaDto> ausencias;
    private List<LicenciaDeptoDto> licencias;

    public DashboardAusenciasResponseDto() {
    }

    public DashboardAusenciasResponseDto(List<DashboardAusenciaDto> ausencias, List<LicenciaDeptoDto> licencias) {
        this.ausencias = ausencias;
        this.licencias = licencias;
    }

    public List<DashboardAusenciaDto> getAusencias() {
        return ausencias;
    }

    public void setAusencias(List<DashboardAusenciaDto> ausencias) {
        this.ausencias = ausencias;
    }

    public List<LicenciaDeptoDto> getLicencias() {
        return licencias;
    }

    public void setLicencias(List<LicenciaDeptoDto> licencias) {
        this.licencias = licencias;
    }
}
