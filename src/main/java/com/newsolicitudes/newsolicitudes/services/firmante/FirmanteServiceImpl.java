package com.newsolicitudes.newsolicitudes.services.firmante;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;

@Service
public class FirmanteServiceImpl implements FirmanteService {

    private final ApiDepartamentoService apiDepartamentoService;
    private final SubroganciaRepository subroganciaRepository;

    public FirmanteServiceImpl(ApiDepartamentoService apiDepartamentoService, SubroganciaRepository subroganciaRepository) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.subroganciaRepository = subroganciaRepository;
    }

    @Override
    public Integer getRutFirmante(Solicitud solicitud) {
        DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());
        while (departamento != null) {
            if (isSigningDepartment(departamento)) {
                if (departamento.getRutJefe().equals(solicitud.getRut()) && departamento.getIdDeptoSuperior() != null) {
                    departamento = apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
                } else {
                    return findSignerInDepartment(departamento, solicitud.getFechaInicio());
                }
            } else if (departamento.getIdDeptoSuperior() != null) {
                departamento = apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
            } else {
                departamento = null;
            }
        }
        return null;
    }

    private boolean isSigningDepartment(DepartamentoResponse departamento) {
        if (departamento == null || departamento.getNivelDepartamento() == null) {
            return false;
        }
        String nivel = departamento.getNivelDepartamento();
        return nivel.equalsIgnoreCase("DIRECCION") || nivel.equalsIgnoreCase("ALCALDIA")
                || nivel.equalsIgnoreCase("ADMINISTRACION") || nivel.equalsIgnoreCase("SUBDIRECCION");
    }

    private Integer findSignerInDepartment(DepartamentoResponse departamento, LocalDate fecha) {
        List<Subrogancia> subrogancias = subroganciaRepository
                .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        departamento.getRutJefe(), fecha, fecha);

        if (!subrogancias.isEmpty()) {
            return subrogancias.get(0).getSubrogante();
        }
        return departamento.getRutJefe();
    }
}
