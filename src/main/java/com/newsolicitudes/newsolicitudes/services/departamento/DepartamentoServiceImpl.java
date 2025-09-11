package com.newsolicitudes.newsolicitudes.services.departamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoList;
import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.apiausencias.ApiAusenciasService;
import com.newsolicitudes.newsolicitudes.services.apidepartamento.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

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
                departamentoActual = apiDepartamentoService
                        .obtenerDepartamento(departamentoInicial.getIdDeptoSuperior());
            } else {
                departamentoActual = departamentoInicial;
            }
        } else {
            departamentoActual = departamentoInicial;
        }

        while (departamentoActual != null) {

            if (departamentoActual.getNivelDepartamento() != null &&
                    departamentoActual.getNivelDepartamento().equalsIgnoreCase("DIRECCION")) {
                if (departamentoActual.getIdDeptoSuperior() != null) {
                    return apiDepartamentoService.obtenerDepartamento(departamentoActual.getIdDeptoSuperior());
                } else {
                    return departamentoActual;
                }
            }

            if (departamentoActual.getRutJefe() != null &&
                    !isAusenteEnFecha(departamentoActual.getRutJefe(), fechaInicio) &&
                    !hasAprobacion(departamentoActual.getRutJefe(), fechaInicio, fechaFin)) {
                return departamentoActual;
            }

            if (departamentoActual.getIdDeptoSuperior() != null) {
                departamentoActual = apiDepartamentoService
                        .obtenerDepartamento(departamentoActual.getIdDeptoSuperior());
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

        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(rut);
        if (funcionario == null) {
            return true;
        }

        return !apiAusenciasService.getAusenciasByRutAndFechas(funcionario.getRut(),
                funcionario.getIdent(), fecha, fecha).isEmpty();

    }

    private boolean hasAprobacion(Integer rut, LocalDate fechaInicio, LocalDate fechaFin) {

        Optional<List<Solicitud>> optSolicitud = solicitudRepository.findByRutAndFechaInicioBetween(rut, fechaInicio,
                fechaFin);

        if (optSolicitud.isEmpty() || optSolicitud.get().isEmpty()) {
            return false;
        }

        // Verificamos si alguna solicitud cumple las condiciones
        return optSolicitud.get().stream()
                .anyMatch(solicitud -> solicitud.getEstado() == Solicitud.EstadoSolicitud.APROBADA &&
                        !solicitud.getFechaInicio().isAfter(fechaFin) &&
                        !solicitud.getFechaTermino().isBefore(fechaInicio));
    }

    @Override
    public List<DepartamentoList> getDepartamentos() {
        return apiDepartamentoService.getDepartamentosList();
    }

}
