package com.newsolicitudes.newsolicitudes.services.derivacioncreate;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(DerivacionCreateServiceImpl.class);

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
        TipoDerivacion tipoDerivacion = determinaTipoDerivacionFinal(departamentoSiguiente, solicitud.getFechaSolicitud());
        logger.info("Tipo de derivacion determinado: {}", tipoDerivacion);

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

    private TipoDerivacion determinaTipoDerivacionFinal(DepartamentoResponse deptoDestino, LocalDate fechaSolicitud) {
        logger.debug("Determinando tipo de derivacion final para deptoDestino: {} (RutJefe: {}), fechaSolicitud: {}",
                deptoDestino.getNombre(), deptoDestino.getRutJefe(), fechaSolicitud);

        // 1. Determinar el tipo por defecto segun el nivel del depto destino
        NivelDepartamento nivelDeptoDestino = DepartamentoUtils.getNivelDepartamento(deptoDestino);
        TipoDerivacion tipoFinal = DepartamentoUtils.tipoPorNivel(nivelDeptoDestino);
        logger.debug("Tipo por defecto segun nivel ({}): {}", nivelDeptoDestino, tipoFinal);

        // 2. Verificar si el jefe del depto destino esta subrogando a un director
        TipoDerivacion tipoPorSubroganciaJefe = getTipoSiJefeEsSubroganteDirector(deptoDestino.getRutJefe(), fechaSolicitud);
        if (tipoPorSubroganciaJefe == TipoDerivacion.FIRMA) {
            logger.debug("Tipo determinado por subrogancia (jefe es subrogante de director): FIRMA");
            return TipoDerivacion.FIRMA;
        }

        // 3. Verificar si el jefe del depto destino esta SIENDO subrogado por un director
        TipoDerivacion tipoPorJefeSubrogado = getTipoSiJefeEstaSiendoSubrogadoPorDirector(deptoDestino.getRutJefe(), fechaSolicitud);
        if (tipoPorJefeSubrogado == TipoDerivacion.FIRMA) {
            logger.debug("Tipo determinado por subrogancia (jefe esta siendo subrogado por director): FIRMA");
            return TipoDerivacion.FIRMA;
        }

        logger.debug("Retornando tipo final por defecto: {}", tipoFinal);
        return tipoFinal;
    }

    private TipoDerivacion getTipoSiJefeEsSubroganteDirector(Integer rutJefe, LocalDate fechaSolicitud) {
        logger.debug("Verificando si jefe {} es subrogante de director para fecha {}", rutJefe, fechaSolicitud);
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, fechaSolicitud, fechaSolicitud);
        logger.debug("Subrogancias encontradas para subrogante {}: {}", rutJefe, subrogancias.size());
        if (subrogancias.isEmpty()) {
            return null;
        }
        for (Subrogancia subrogancia : subrogancias) {
            logger.debug("Evaluando subrogancia ID: {}, subrogante: {}, jefeDepartamento: {}, idDepto: {}",
                    subrogancia.getId(), subrogancia.getSubrogante(), subrogancia.getJefeDepartamento(), subrogancia.getIdDepto());
            DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
            if (deptoSubrogado != null) {
                NivelDepartamento nivel = DepartamentoUtils.getNivelDepartamento(deptoSubrogado);
                logger.debug("Nivel de departamento subrogado ({}): {}", deptoSubrogado.getNombre(), nivel);
                if (nivel == NivelDepartamento.DIRECCION ||
                    nivel == NivelDepartamento.SUBDIRECCION ||
                    nivel == NivelDepartamento.ADMINISTRACION ||
                    nivel == NivelDepartamento.ALCALDIA) {
                    logger.debug("Nivel de departamento subrogado ({}) implica FIRMA por regla explicita.", nivel);
                    return TipoDerivacion.FIRMA;
                }
                if (DepartamentoUtils.tipoPorNivel(nivel) == TipoDerivacion.FIRMA) {
                    logger.debug("Nivel de departamento subrogado ({}) implica FIRMA por tipoPorNivel.", nivel);
                    return TipoDerivacion.FIRMA;
                }
            } else {
                logger.warn("Departamento subrogado con ID {} no encontrado.", subrogancia.getIdDepto());
            }
        }
        logger.debug("Ninguna subrogancia activa para jefe {} implica FIRMA.", rutJefe);
        return null;
    }

    private TipoDerivacion getTipoSiJefeEstaSiendoSubrogadoPorDirector(Integer rutJefeOriginal, LocalDate fechaSolicitud) {
        logger.debug("Verificando si jefe {} esta siendo subrogado por director para fecha {}", rutJefeOriginal, fechaSolicitud);
        List<Subrogancia> subrogancias = subroganciaRepository.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeOriginal, fechaSolicitud, fechaSolicitud);
        logger.debug("Subrogancias encontradas donde jefe {} esta siendo subrogado: {}", rutJefeOriginal, subrogancias.size());
        if (subrogancias.isEmpty()) {
            return null;
        }
        for (Subrogancia subrogancia : subrogancias) {
            logger.debug("Evaluando subrogancia ID: {}, subrogante: {}, jefeDepartamento: {}, idDepto: {}",
                    subrogancia.getId(), subrogancia.getSubrogante(), subrogancia.getJefeDepartamento(), subrogancia.getIdDepto());
            Integer rutSubrogante = subrogancia.getSubrogante();
            FuncionarioResponseApi funcionarioSubrogante = funcionarioService.getFuncionarioByRut(rutSubrogante);
            if (funcionarioSubrogante != null) {
                DepartamentoResponse deptoSubrogante = departamentoService.getDepartamentoById(funcionarioSubrogante.getCodDepto());
                if (deptoSubrogante != null) {
                    NivelDepartamento nivelSubrogante = DepartamentoUtils.getNivelDepartamento(deptoSubrogante);
                    logger.debug("Nivel de departamento de subrogante ({}): {}", deptoSubrogante.getNombre(), nivelSubrogante);
                    if (DepartamentoUtils.tipoPorNivel(nivelSubrogante) == TipoDerivacion.FIRMA) {
                        logger.debug("Nivel de departamento de subrogante ({}) implica FIRMA.", nivelSubrogante);
                        return TipoDerivacion.FIRMA;
                    }
                } else {
                    logger.warn("Departamento de subrogante con ID {} no encontrado.", funcionarioSubrogante.getCodDepto());
                }
            } else {
                logger.warn("Funcionario subrogante con rut {} no encontrado.", rutSubrogante);
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