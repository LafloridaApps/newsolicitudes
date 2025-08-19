package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiAusenciasService;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.FuncionarioService;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private final ApiDepartamentoService apiDepartamentoService;

    private final ApiAusenciasService apiAusenciasService;

    private final FuncionarioService funcionarioService;


    private final SolicitudRepository solicitudRepository;

    public DepartamentoServiceImpl(ApiDepartamentoService apiDepartamentoService,
            ApiAusenciasService apiAusenciasService, FuncionarioService funcionarioService,
            SolicitudRepository solicitudRepository) {
        this.apiDepartamentoService = apiDepartamentoService;
        this.apiAusenciasService = apiAusenciasService;
        this.funcionarioService = funcionarioService;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public DepartamentoResponse getDepartamentoDestino(Integer rutSolicitante, DepartamentoResponse departamentoInicial,
            LocalDate fechaInicio, LocalDate fechaFin) {

        DepartamentoResponse departamentoActual;

        if (rutSolicitante.equals(departamentoInicial.getRutJefe())) {
            if (departamentoInicial.getIdDeptoSuperior() != null) {
                departamentoActual = apiDepartamentoService.obtenerDepartamento(departamentoInicial.getIdDeptoSuperior());
            } else {
                departamentoActual = departamentoInicial;
            }
        } else {
            departamentoActual = departamentoInicial;
        }

        while (departamentoActual != null) {
            if (departamentoActual.getRutJefe() != null &&
                !isAusenteEnFecha(departamentoActual.getRutJefe(), fechaInicio) &&
                !hasAprobacion(departamentoActual.getRutJefe(), fechaInicio, fechaFin)) {
                return departamentoActual;
            }

            if (departamentoActual.getNivelDepartamento() != null &&
                departamentoActual.getNivelDepartamento().equalsIgnoreCase("DIRECCION")) {
                return departamentoActual;
            }

            if (departamentoActual.getIdDeptoSuperior() != null) {
                departamentoActual = apiDepartamentoService.obtenerDepartamento(departamentoActual.getIdDeptoSuperior());
            } else {
                return departamentoActual;
            }
        }
        return null;
    }

    @Override
    public DepartamentoResponse getDepartamentoById(Long id) {
        return apiDepartamentoService.obtenerDepartamento(id);
    }

    private boolean isAusenteEnFecha(Integer rut, LocalDate fecha) {

        FuncionarioResponse funcionario = funcionarioService.getFuncionarioByRut(rut);
        if (funcionario == null) {
            return true;
        }

        return !apiAusenciasService.getAusenciasByRutAndFechas(funcionario.getRut(),
                funcionario.getIdent(), fecha, fecha).isEmpty();

    }

    private boolean hasAprobacion(Integer rut, LocalDate fechaInicio, LocalDate fechaFin) {

        Optional<Solicitud> optSolicitud = solicitudRepository.findByRutAndFechaInicioBetween(rut, fechaInicio,
                fechaFin);

        if (optSolicitud.isEmpty()) {
            return false;
        } else {
            Solicitud solicitud = optSolicitud.get();
            return solicitud.getEstado() == Solicitud.EstadoSolicitud.APROBADA &&
                   !solicitud.getFechaInicio().isAfter(fechaFin) &&
                   !solicitud.getFechaTermino().isBefore(fechaInicio);
        }
    }

}
