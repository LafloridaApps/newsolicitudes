package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class DerivacionServiceImpl implements DerivacionService {

    private final DerivacionRepository derivacionRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final DepartamentoRepository departamentoRepository;

    public DerivacionServiceImpl(
            DerivacionRepository derivacionRepository,
            FuncionarioRepository funcionarioRepository,DepartamentoRepository departamentoRepository) {
        this.derivacionRepository = derivacionRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.departamentoRepository = departamentoRepository;
    }

    @Override
    @Transactional(rollbackFor = DerivacionExceptions.class)
    public Derivacion createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo,
            Departamento departamento, EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions {

        if (departamento == null) {
            throw new DerivacionExceptions("El departamento de destino no puede ser nulo");
        }

        Derivacion derivacionInicial = new Derivacion();
        derivacionInicial.setSolicitud(solicitud);
        derivacionInicial.setDepartamento(departamento);
        derivacionInicial.setFechaDerivacion(LocalDate.now());
        derivacionInicial.setEstadoDerivacion(estadoDerivacion);
        derivacionInicial.setTipo(tipo);

        return derivacionRepository.save(derivacionInicial);
    }

    @Override
   public List<SolicitudDto> getDerivacionesByFuncionario(Integer rut) {
    Funcionario funcionario = getFuncionarioBuRut(rut);

    List<Departamento> departamentos = departamentoRepository.findByFuncionarios(funcionario);

    List<Derivacion> derivaciones = derivacionRepository.findByDepartamentoIn(departamentos);

    Map<Long, List<Derivacion>> agrupadasPorSolicitud = derivaciones.stream()
        .collect(Collectors.groupingBy(d -> d.getSolicitud().getId()));

    return agrupadasPorSolicitud.entrySet().stream().map(entry -> {
        Derivacion primera = entry.getValue().get(0);
        Solicitud solicitud = primera.getSolicitud();

        SolicitudDto dto = new SolicitudDto();
        dto.setId(solicitud.getId());
        dto.setSolicitante(solicitud.getNombreSolicitante());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud().toString());
        dto.setFechaInicio(solicitud.getFechaInicio().toString());
        dto.setFechaFin(solicitud.getFechaTermino().toString());
        dto.setTipoSolicitud(solicitud.getTipoSolicitud().toString());
        dto.setEstadoSolicitud(solicitud.getEstado().toString());
        dto.setJornadaInicio(solicitud.getJornadaInicio() != null ? solicitud.getJornadaInicio().toString() : null);
        dto.setJornadaFin(solicitud.getJornadaTermino() != null ? solicitud.getJornadaTermino().toString() : null);
        dto.setDepartamentoOrigen(solicitud.getNombreDepartamento());

        List<DerivacionDto> derivacionDtos = entry.getValue().stream().map(d -> {
            DerivacionDto ddto = new DerivacionDto();
            ddto.setId(d.getId());
            ddto.setTipoMovimiento(d.getTipo().toString());
            ddto.setFechaDerivacion(d.getFechaDerivacion().toString());
            ddto.setNombreDepartamento(d.getNombreDepartamento());
            ddto.setEstadoDerivacion(d.getEstadoDerivacion().name());

            // Indicar si este funcionario ya recepcionó esta derivación
            boolean recepcionada = d.getEntrada() != null && 
                                   d.getEntrada().getFuncionario() != null && 
                                   d.getEntrada().getFuncionario().equals(funcionario);
            ddto.setRecepcionada(recepcionada);

            if (d.getEntrada() != null) {
                ddto.setJefeDestino(d.getEntrada().getFuncionario().getNombre());
            }

            return ddto;
        }).toList();

        dto.setDerivaciones(derivacionDtos);

        return dto;
    }).toList();
}


    private Funcionario getFuncionarioBuRut(Integer rut) {
        return RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                String.format("No existe el funcionario %d", rut));
    }
}
