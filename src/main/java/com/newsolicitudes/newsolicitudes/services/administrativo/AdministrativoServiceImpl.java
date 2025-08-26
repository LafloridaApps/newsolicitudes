package com.newsolicitudes.newsolicitudes.services.administrativo;

import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.AdministrativoDto;
import com.newsolicitudes.newsolicitudes.dto.ApiAdministrativoResponse;
import com.newsolicitudes.newsolicitudes.services.apiadministrativo.ApiAdmistrativoService;

@Service
public class AdministrativoServiceImpl implements AdministrativoService {

    private final ApiAdmistrativoService apiAdmistrativoService;

    public AdministrativoServiceImpl(ApiAdmistrativoService apiAdmistrativoService) {
        this.apiAdmistrativoService = apiAdmistrativoService;
    }

    @Override
    public AdministrativoDto getAdministrativoByRutAndIdent(Integer rut, Integer ident) {

        ApiAdministrativoResponse response = apiAdmistrativoService.obtenerAdministrativos(rut, ident);

        List<AdministrativoDto.Detalle> detalles = convertirADetalle(response.getDetalle());

        return AdministrativoDto.builder()
                .maximo(response.getMaximo())
                .usados(response.getUsados())
                .saldo(response.getSaldo())
                .anio(response.getAnio())
                .detalle(detalles)
                .build();

    }

    private List<AdministrativoDto.Detalle> convertirADetalle(List<ApiAdministrativoResponse.Detalle> detalle) {
        return detalle.stream()
                .map(d -> new AdministrativoDto.Detalle(
                        d.getNumero(),
                        d.getResolucion(),
                        d.getFechaResolucion(),
                        d.getFechaInicio(),
                        d.getFechaTermino(),
                        d.getPeriodo()))
                .toList();
    }

}
