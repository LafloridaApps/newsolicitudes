package com.newsolicitudes.newsolicitudes.services;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiFuncionarioService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DerivacionServiceImpl implements DerivacionService {

    private final DerivacionRepository derivacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final ApiFuncionarioService apiFuncionarioService;
    private final ApiDepartamentoService apiDepartamentoService;

    public DerivacionServiceImpl(
            DerivacionRepository derivacionRepository,
            SolicitudRepository solicitudRepository,
            ApiFuncionarioService apiFuncionarioService,
            ApiDepartamentoService apiDepartamentoService) {
        this.derivacionRepository = derivacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.apiFuncionarioService = apiFuncionarioService;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    @Transactional(rollbackFor = DerivacionExceptions.class)
    public void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo,
            Long idDpeto, EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions {

        Derivacion derivacionInicial = new Derivacion();
        derivacionInicial.setSolicitud(solicitud);
        derivacionInicial.setIdDepto(idDpeto);
        derivacionInicial.setFechaDerivacion(LocalDate.now());
        derivacionInicial.setEstadoDerivacion(estadoDerivacion);
        derivacionInicial.setTipo(tipo);

        derivacionRepository.save(derivacionInicial);
    }

    @Override
    public List<SolicitudDto> getDerivacionesByDeptoId(Long idDepto) {
        List<Derivacion> derivaciones = derivacionRepository.findByIdDeptoAndEstadoDerivacion(idDepto, EstadoDerivacion.PENDIENTE);
        List<SolicitudDto> solicitudesDto = new ArrayList<>();

        for (Derivacion derivacion : derivaciones) {
            Optional<Solicitud> solicitudOptional = solicitudRepository.findById(derivacion.getSolicitud().getId());
            solicitudOptional.ifPresent(solicitud -> {
                SolicitudDto dto = new SolicitudDto();
                dto.setId(solicitud.getId());
                dto.setSolicitante(String.valueOf(solicitud.getRut()));
                dto.setFechaSolicitud(solicitud.getFechaSolicitud().toString());
                dto.setFechaInicio(solicitud.getFechaInicio().toString());
                dto.setFechaFin(solicitud.getFechaTermino().toString());
                dto.setJornadaInicio(solicitud.getJornadaInicio() != null ? solicitud.getJornadaInicio().name() : "");
                dto.setJornadaFin(solicitud.getJornadaTermino() != null ? solicitud.getJornadaTermino().name() : "");
                dto.setTipoSolicitud(solicitud.getTipoSolicitud().name());
                dto.setDepartamentoOrigen(String.valueOf(solicitud.getIdDepto()));
                dto.setEstadoSolicitud(solicitud.getEstado().name());
                dto.setCantidadDias(solicitud.getCantidadDias());

                // Obtener nombre del funcionario
                FuncionarioResponse funcionario = apiFuncionarioService.obtenerDetalleColaborador(solicitud.getRut());
                if (funcionario != null) {
                    dto.setNombreFuncionario(funcionario.getNombre() + " " + funcionario.getApellidoPaterno() + " " + funcionario.getApellidoMaterno());
                }

                // Obtener nombre del departamento
                DepartamentoResponse departamento = apiDepartamentoService.obtenerDepartamento(solicitud.getIdDepto());
                if (departamento != null) {
                    dto.setNombreDepartamento(departamento.getNombre());
                }

                solicitudesDto.add(dto);
            });
        }
        return solicitudesDto;
    }
}
