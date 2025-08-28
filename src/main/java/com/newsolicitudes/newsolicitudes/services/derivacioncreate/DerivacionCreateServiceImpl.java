package com.newsolicitudes.newsolicitudes.services.derivacioncreate;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.derivacion.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.visacion.VisacionService;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class DerivacionCreateServiceImpl implements DerivacionCreateService {

    private final DerivacionService derivacionService;
    private final DerivacionRepository derivacionRepository;
    private final VisacionService visacionService;
    private final DepartamentoService departamentoService;
    private final SubroganciaRepository subroganciaRepository;
    private final FuncionarioService funcionarioService;

    public DerivacionCreateServiceImpl(DerivacionService derivacionService, DerivacionRepository derivacionRepository,
            VisacionService visacionService, DepartamentoService departamentoService,
            SubroganciaRepository subroganciaRepository, FuncionarioService funcionarioService) {
        this.derivacionService = derivacionService;
        this.derivacionRepository = derivacionRepository;
        this.visacionService = visacionService;
        this.departamentoService = departamentoService;
        this.subroganciaRepository = subroganciaRepository;
        this.funcionarioService = funcionarioService;
    }

    @Override
    public void createDerivacionDepto(Long idDerivacion, Integer rut) {

        Derivacion derivacion = getDerivacionBydId(idDerivacion);
        Solicitud solicitud = derivacion.getSolicitud();
        Long departamento = derivacion.getIdDepto();
        DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(departamento);
        DepartamentoResponse departamentoSiguiente = departamentoService.getDepartamentoDestino(
                departamentoActual.getRutJefe(),
                departamentoActual, solicitud.getFechaInicio(), solicitud.getFechaTermino());

        Long departamentoSiguienteId = departamentoSiguiente.getId();

        // Logica de tipo de derivacion
        TipoDerivacion tipoDerivacion = determinaTipoDerivacionFinal(departamentoSiguiente);

        EstadoDerivacion estadoDerivacion = estadoDerivacion(derivacion);
        if (estadoDerivacion == EstadoDerivacion.DERIVADA
                || DepartamentoUtils.getNivelDepartamento(departamentoSiguiente) == NivelDepartamento.DEPARTAMENTO
                || DepartamentoUtils.getNivelDepartamento(departamentoSiguiente) == NivelDepartamento.SECCION
                || DepartamentoUtils.getNivelDepartamento(departamentoSiguiente) == NivelDepartamento.OFICINA) {

            visacionService.visarSolicitud(solicitud, departamentoActual.getRutJefeSuperior());
        }
        derivacion.setEstadoDerivacion(estadoDerivacion);
        derivacionRepository.save(derivacion);

        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamentoSiguienteId,
                EstadoDerivacion.PENDIENTE);
    }

    private TipoDerivacion determinaTipoDerivacionFinal(DepartamentoResponse deptoDestino) {
        // 1. Determinar el tipo por defecto segun el nivel del depto destino
        NivelDepartamento nivelDeptoDestino = DepartamentoUtils.getNivelDepartamento(deptoDestino);
        TipoDerivacion tipoFinal = DepartamentoUtils.tipoPorNivel(nivelDeptoDestino);

        // 2. Verificar si el jefe del depto destino esta subrogando a un director
        TipoDerivacion tipoPorSubroganciaJefe = getTipoSiJefeEsSubroganteDirector(deptoDestino.getRutJefe());
        if (tipoPorSubroganciaJefe == TipoDerivacion.FIRMA) {
            return TipoDerivacion.FIRMA;
        }

        // 3. Verificar si el jefe del depto destino esta SIENDO subrogado por un director
        TipoDerivacion tipoPorJefeSubrogado = getTipoSiJefeEstaSiendoSubrogadoPorDirector(deptoDestino.getRutJefe());
        if (tipoPorJefeSubrogado == TipoDerivacion.FIRMA) {
            return TipoDerivacion.FIRMA;
        }

        return tipoFinal;
    }

    private TipoDerivacion getTipoSiJefeEsSubroganteDirector(Integer rutJefe) {
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, LocalDate.now(), LocalDate.now());
        if (subrogancias.isEmpty()) {
            return null;
        }
        for (Subrogancia subrogancia : subrogancias) {
            DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
            NivelDepartamento nivel = DepartamentoUtils.getNivelDepartamento(deptoSubrogado);
            if (DepartamentoUtils.tipoPorNivel(nivel) == TipoDerivacion.FIRMA) {
                return TipoDerivacion.FIRMA;
            }
        }
        return null;
    }

    private TipoDerivacion getTipoSiJefeEstaSiendoSubrogadoPorDirector(Integer rutJefeOriginal) {
        List<Subrogancia> subrogancias = subroganciaRepository.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeOriginal, LocalDate.now(), LocalDate.now());
        if (subrogancias.isEmpty()) {
            return null;
        }
        for (Subrogancia subrogancia : subrogancias) {
            Integer rutSubrogante = subrogancia.getSubrogante();
            FuncionarioResponseApi funcionarioSubrogante = funcionarioService.getFuncionarioByRut(rutSubrogante);
            DepartamentoResponse deptoSubrogante = departamentoService.getDepartamentoById(funcionarioSubrogante.getCodDepto());
            NivelDepartamento nivelSubrogante = DepartamentoUtils.getNivelDepartamento(deptoSubrogante);
            if (DepartamentoUtils.tipoPorNivel(nivelSubrogante) == TipoDerivacion.FIRMA) {
                return TipoDerivacion.FIRMA;
            }
        }
        return null;
    }

    private Derivacion getDerivacionBydId(Long id) {
        return RepositoryUtils.findOrThrow(derivacionRepository.findById(id),
                String.format("No Existe la derivacion %d", id));
    }

    private EstadoDerivacion estadoDerivacion(Derivacion derivacion) {
        if (derivacion.getEstadoDerivacion() == null) {
            return EstadoDerivacion.PENDIENTE;
        } else if (derivacion.getEstadoDerivacion() == EstadoDerivacion.PENDIENTE) {
            return EstadoDerivacion.DERIVADA;
        }
        return EstadoDerivacion.PENDIENTE;
    }
}