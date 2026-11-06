package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.ExisteSolicitudResponseDto;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.MiSolicitudDto;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.PageMiSolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDetalleDto;
import com.newsolicitudes.newsolicitudes.dto.SolicitudRequest;
import com.newsolicitudes.newsolicitudes.dto.SolicitudResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.dto.Trazabilidad;
import com.newsolicitudes.newsolicitudes.dto.UpdateSolicitudRequest;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Postergacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.TipoSolicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.exceptions.NotFoundException;
import com.newsolicitudes.newsolicitudes.exceptions.SolicitudException;
import com.newsolicitudes.newsolicitudes.exceptions.SubroganciaException;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.PostergacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.mappers.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.services.calculodias.CalculadoraDiasService;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.derivacion.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;
import com.newsolicitudes.newsolicitudes.services.trazabilidad.TrazabilidadService;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudServiceImpl.class);

    private final FuncionarioService funcionarioService;
    private final SolicitudRepository solicitudRepository;
    private final DerivacionService derivacionService;
    private final SubroganciaService subroganciaService;
    private final DepartamentoService departamentoService;
    private final SolicitudMapper solicitudMapper;
    private final AprobacionRepository aprobacionRepository;
    private final PostergacionRepository postergacionRepository;
    private final SubroganciaRepository subroganciaRepository;
    private final CalculadoraDiasService calculadoraDiasService;
    private final TrazabilidadService trazabilidadService;
    private final SolicitudAnulacionGestor solicitudAnulacionGestor;
    private final SolicitudNotificador solicitudNotificador;

    public SolicitudServiceImpl(DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            SolicitudMapper solicitudMapper,
            AprobacionRepository aprobacionRepository,
            PostergacionRepository postergacionRepository,
            SubroganciaRepository subroganciaRepository,
            CalculadoraDiasService calculadoraDiasService,
            TrazabilidadService trazabilidadService,
            SolicitudAnulacionGestor solicitudAnulacionGestor,
            SolicitudNotificador solicitudNotificador) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.solicitudMapper = solicitudMapper;
        this.aprobacionRepository = aprobacionRepository;
        this.postergacionRepository = postergacionRepository;
        this.subroganciaRepository = subroganciaRepository;
        this.calculadoraDiasService = calculadoraDiasService;
        this.trazabilidadService = trazabilidadService;
        this.solicitudAnulacionGestor = solicitudAnulacionGestor;
        this.solicitudNotificador = solicitudNotificador;
    }

    // Record privado para encapsular el resultado de la lógica de enrutamiento.
    private record RutaDerivacion(DepartamentoResponse departamentoDestino, TipoDerivacion tipoDerivacion) {
    }

    // Orquesta el proceso completo de creación de una nueva solicitud.
    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {
        if (request == null || request.getRut() == null) {
            throw new SolicitudException("La solicitud o el RUT no pueden ser nulos.");
        }

        logger.info("Iniciando creación de solicitud para RUT: {}, tipo: {}", request.getRut(), request.getTipoSolicitud());
        try {
            // 1. Obtener datos iniciales del funcionario y su departamento.
            FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(request.getRut());
            DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(funcionario.getCodDepto());

            if (buscarSolicitudesPendientesAprobacion(request.getTipoSolicitud(), request.getRut())) {
                throw new SolicitudException(
                        "Tiene un formulario pendiente de firma.");
            }

            // 2. Determinar la ruta de derivación (departamento destino y tipo).
            RutaDerivacion ruta = determinarRutaDerivacion(request, departamentoActual);

            // 3. Calcular días y crear la entidad Solicitud principal.
            double cantidadDias = calculadoraDiasService.calcularDias(request);
            Solicitud solicitud = crearYGuardarSolicitud(request, funcionario.getRut(), funcionario.getCodDepto(),
                    cantidadDias);

            // 4. Crear entidades relacionadas (derivación y subrogancia).
            derivacionService.createSolicitudDerivacion(solicitud, ruta.tipoDerivacion(),
                    ruta.departamentoDestino().getId(),
                    EstadoDerivacion.PENDIENTE);

            String mensajeSubrogancia = procesarSubroganciaSiCorresponde(request);

            // 5. Enviar notificación de la nueva solicitud.
            solicitudNotificador.enviarNotificacionNuevaSolicitud(solicitud, ruta.departamentoDestino(), funcionario, departamentoActual.getNombre());

            logger.info("Solicitud creada exitosamente ID: {} para RUT: {}", solicitud.getId(), request.getRut());
            SolicitudResponse response = new SolicitudResponse(solicitud.getId(), ruta.departamentoDestino().getNombre());
            if (mensajeSubrogancia != null) {
                response.setMensaje(mensajeSubrogancia);
            }
            return response;
        } catch (SolicitudException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            String causaStr = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new SolicitudException(String.format("Error al procesar la creacion de la solicitud para RUT %s: %s", request.getRut(), causaStr), e);
        }
    }

    // Procesa la creación de la subrogancia si el request la incluye y no existe previamente en el período.
    private String procesarSubroganciaSiCorresponde(SolicitudRequest request) {
        if (request.getSubrogancia() == null) {
            return null;
        }

        Integer rutJefe = request.getSubrogancia().getRutJefe() != null
                ? request.getSubrogancia().getRutJefe()
                : request.getRut();
        Long idDepto = request.getDepto();
        LocalDate fechaInicio = request.getFechaInicio();
        LocalDate fechaFin = request.getFechaFin();

        boolean yaExisteSubrogancia = subroganciaRepository
                .existeSubrogancia(rutJefe, fechaInicio, fechaFin, idDepto)
                .isPresent();

        if (yaExisteSubrogancia) {
            logger.info("El RUT {} ya tiene una subrogancia creada para el depto {} en el período {} - {}. Se omite grabación de subrogancia.",
                    rutJefe, idDepto, fechaInicio, fechaFin);
            return "No se generó subrogancia porque ya tenía una creada en el período.";
        }

        try {
            createSubroganciaSol(request.getSubrogancia(), fechaInicio, fechaFin, idDepto);
        } catch (SubroganciaException e) {
            logger.info("Subrogancia previa detectada por SubroganciaException: {}", e.getMessage());
            return "No se generó subrogancia porque ya tenía una creada en el período.";
        }

        return null;
    }

    // Determina el departamento de destino y el tipo de derivación para una
    // solicitud.
    private RutaDerivacion determinarRutaDerivacion(SolicitudRequest request, DepartamentoResponse deptoActual) {
        DepartamentoResponse departamentoDestino = departamentoService.getDepartamentoDestino(request.getRut(),
                deptoActual, FechaUtils.fechaActual(), FechaUtils.fechaActual());
        String tipoContrato = funcionarioService.getFuncionarioByRut(request.getRut()).getTipoContrato();
        NivelDepartamento nivelDepartamento = DepartamentoUtils.getNivelDepartamento(departamentoDestino);
        TipoDerivacion tipoDerivacion = DepartamentoUtils.tipoPorNivel(nivelDepartamento, tipoContrato);

        // Validar si el jefe del departamento destino está subrogando a un cargo con
        // nivel de FIRMA
        // para otorgarle el permiso resolutivo en lugar de solo VISACION inicial.
        if (departamentoDestino.getRutJefe() != null) {
            LocalDate hoy = FechaUtils.fechaActual();
            List<Subrogancia> subrogancias = subroganciaRepository
                    .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                            departamentoDestino.getRutJefe(), hoy, hoy);

            for (Subrogancia subrogancia : subrogancias) {
                DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
                if (deptoSubrogado != null) {
                    NivelDepartamento nivelSubrogado = DepartamentoUtils.getNivelDepartamento(deptoSubrogado);
                    TipoDerivacion tipoSubrogado = DepartamentoUtils.tipoPorNivel(nivelSubrogado, tipoContrato);
                    if (tipoSubrogado == TipoDerivacion.FIRMA) {
                        tipoDerivacion = TipoDerivacion.FIRMA;
                        break;
                    }
                }
            }
        }

        return new RutaDerivacion(departamentoDestino, tipoDerivacion);
    }

    // Mapea los datos de la solicitud a una entidad y la persiste en la base de
    // datos.
    private Solicitud crearYGuardarSolicitud(SolicitudRequest request, int rutFuncionario, long idDepto,
            double cantidadDias) {
        Solicitud solicitud = solicitudMapper.solicitudRequestToSolicitud(request, rutFuncionario,
                idDepto, cantidadDias);
        return solicitudRepository.save(solicitud);
    }

    // Llama al servicio de subrogancia para crear un registro de subrogancia
    // asociado a la solicitud.
    private void createSubroganciaSol(SubroganciaRequest subrogancia, LocalDate fechaInicio, LocalDate fechaFin,
            Long idDepto) {
        subroganciaService.createSubrogancia(subrogancia, fechaInicio, fechaFin, idDepto);
    }

    // Verifica si ya existe una solicitud para un funcionario en un rango de fechas
    // determinado.
    @Override
    public ExisteSolicitudResponseDto existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo) {
        try {
            Optional<Solicitud> solicitudOptional = solicitudRepository
                    .findFirstByRutAndTipoSolicitudAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
                            rut, Solicitud.TipoSolicitud.valueOf(tipo), fechaInicio, fechaInicio);

            return solicitudOptional
                    .map(solicitudMapper::solicitudToExisteSolicitudResponseDto)
                    .orElse(new ExisteSolicitudResponseDto(false, null, null, null, null, null));
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al verificar existencia de solicitud para RUT %s y fecha %s", rut, fechaInicio), e);
        }
    }

    // Obtiene una lista paginada de las solicitudes de un funcionario.
    @Override
    public PageMiSolicitudResponse getSolicitudesByRut(Integer rut, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Solicitud::getId).descending());
            Page<Solicitud> solicitudes = solicitudRepository.findByRut(rut, pageable);
            List<MiSolicitudDto> miSolicitudes = solicitudes.getContent().stream()
                    .map(this::mapToMiSolicitudDto)
                    .toList();

            PageMiSolicitudResponse pageMiSolicitudResponse = new PageMiSolicitudResponse();
            pageMiSolicitudResponse.setSolicitudes(miSolicitudes);
            pageMiSolicitudResponse.setTotalPages(solicitudes.getTotalPages());
            pageMiSolicitudResponse.setTotalElements(solicitudes.getTotalElements());
            pageMiSolicitudResponse.setCurrentPage(solicitudes.getNumber());
            pageMiSolicitudResponse.setPageSize(solicitudes.getSize());

            return pageMiSolicitudResponse;
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al obtener solicitudes paginadas para RUT %s", rut), e);
        }
    }

    // Mapea una entidad Solicitud al DTO utilizado en la lista "Mis Solicitudes".
    private MiSolicitudDto mapToMiSolicitudDto(Solicitud solicitud) {
        String urlPdf = getUrlPdf(solicitud);
        List<Trazabilidad> trazabilidadList = trazabilidadService.construirTrazabilidad(solicitud);
        return solicitudMapper.solicitudToMiSolicitudDto(solicitud, urlPdf, trazabilidadList);
    }

    // Obtiene los detalles completos de una solicitud específica por su ID.
    @Override
    public SolicitudDetalleDto getSolicitudDetalleById(Long idSolicitud) {
        try {
            Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                    .orElseThrow(() -> new NotFoundException("Solicitud no encontrada con id: " + idSolicitud));

            FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(solicitud.getRut());
            String nombreFuncionario = funcionario.getNombreCompleto();

            DepartamentoResponse departamento = departamentoService.getDepartamentoById(solicitud.getIdDepto());
            String nombreDepartamento = departamento.getNombre();

            String urlPdf = getUrlPdf(solicitud);

            List<DerivacionDto> derivaciones = solicitud.getDerivaciones().stream()
                    .map(this::mapToDerivacionDto)
                    .toList();

            return solicitudMapper.solicitudToSolicitudDetalleDto(solicitud, nombreFuncionario, nombreDepartamento,
                    urlPdf, derivaciones);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al obtener detalle de la solicitud ID %s", idSolicitud), e);
        }
    }

    // Mapea una entidad Derivacion a su DTO, enriqueciendo con el nombre del
    // departamento.
    private DerivacionDto mapToDerivacionDto(Derivacion derivacion) {
        DepartamentoResponse depto = departamentoService.getDepartamentoById(derivacion.getIdDepto());
        return solicitudMapper.derivacionToDerivacionDto(derivacion, depto.getNombre());
    }

    // Obtiene la URL del PDF de una solicitud si ya ha sido aprobada.
    private String getUrlPdf(Solicitud solicitud) {
        Optional<Aprobacion> optAprobacion = aprobacionRepository.findBySolicitud(solicitud);
        return optAprobacion.map(Aprobacion::getUrlPdf).orElse(null);
    }

    // Orquesta la actualización de una solicitud existente.
    @Override
    @Transactional
    public void updateSolicitud(Long idSolicitud, UpdateSolicitudRequest request) {
        try {
            Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                    .orElseThrow(() -> new NotFoundException("Solicitud no encontrada con id: " + idSolicitud));

            actualizarFechasSiEsNecesario(solicitud, request);
            gestionarCambioDeEstado(solicitud, request);

            solicitudRepository.save(solicitud);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al actualizar la solicitud ID %s", idSolicitud), e);
        }
    }

    // Actualiza las fechas de la solicitud y recalcula los días si han cambiado.
    private void actualizarFechasSiEsNecesario(Solicitud solicitud, UpdateSolicitudRequest request) {
        boolean datesUpdated = false;
        if (request.getFechaInicio() != null && !request.getFechaInicio().equals(solicitud.getFechaInicio())) {
            solicitud.setFechaInicio(request.getFechaInicio());
            datesUpdated = true;
        }
        if (request.getFechaFin() != null && !request.getFechaFin().equals(solicitud.getFechaTermino())) {
            solicitud.setFechaTermino(request.getFechaFin());
            datesUpdated = true;
        }

        if (datesUpdated) {
            SolicitudRequest solicitudRequest = new SolicitudRequest();
            solicitudRequest.setFechaInicio(solicitud.getFechaInicio());
            solicitudRequest.setFechaFin(solicitud.getFechaTermino());
            solicitudRequest.setTipoSolicitud(solicitud.getTipoSolicitud().name());
            solicitudRequest.setJornadaInicio(solicitud.getJornadaInicio().name());
            solicitudRequest.setJornadaTermino(solicitud.getJornadaTermino().name());

            double cantidadDias = calculadoraDiasService.calcularDias(solicitudRequest);
            solicitud.setCantidadDias(cantidadDias);
        }
    }

    // Gestiona la transición de estado de una solicitud, incluyendo el caso
    // especial de postergación.
    private void gestionarCambioDeEstado(Solicitud solicitud, UpdateSolicitudRequest request) {
        if (request.getEstadoSolicitud() == null) {
            return;
        }

        Solicitud.EstadoSolicitud nuevoEstado = Solicitud.EstadoSolicitud.valueOf(request.getEstadoSolicitud());
        if (nuevoEstado == solicitud.getEstado()) {
            return;
        }

        if (nuevoEstado == Solicitud.EstadoSolicitud.POSTERGADA) {
            Aprobacion aprobacion = aprobacionRepository.findBySolicitud(solicitud)
                    .orElseThrow(
                            () -> new NotFoundException(
                                    "No se puede postergar una solicitud que no ha sido aprobada."));

            Postergacion postergacion = new Postergacion();
            postergacion.setFechaPostergacion(FechaUtils.fechaActual());
            postergacion.setRutPostergacion(aprobacion.getRut());
            postergacion.setSolicitud(solicitud);
            postergacion.setGlosa("Postergación a través del mantenedor de solicitudes.");
            postergacionRepository.save(postergacion);
        }
        solicitud.setEstado(nuevoEstado);
    }

    @Override
    public boolean buscarSolicitudesPendientesAprobacion(String tipoSolicitud, Integer rutFuncionario) {
        try {
            TipoSolicitud tipoSol = (tipoSolicitud != null) ? TipoSolicitud.valueOf(tipoSolicitud) : null;

            List<Solicitud> solicitudesPendientes = solicitudRepository
                    .findByTipoSolicitudAndEstadoAndRut(tipoSol, EstadoSolicitud.PENDIENTE, rutFuncionario);
            return !solicitudesPendientes.isEmpty();
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al buscar solicitudes pendientes para RUT %s y tipo %s", rutFuncionario, tipoSolicitud), e);
        }
    }

    @Override
    @Transactional
    public String anularSolicitud(Long idSolicitud, String motivo) {
        try {
            Solicitud solicitud = getSolicitudById(idSolicitud);
            return solicitudAnulacionGestor.anular(solicitud, motivo);
        } catch (NotFoundException | com.newsolicitudes.newsolicitudes.exceptions.AnulacionException e) {
            throw e;
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al procesar la anulacion de la solicitud ID %s", idSolicitud), e);
        }
    }

    @Override
    @Transactional
    public String resolverSolicitudAnulacion(Long idSolicitud, Integer rutAprobador) {
        try {
            return solicitudAnulacionGestor.resolverAnulacion(idSolicitud, rutAprobador);
        } catch (NotFoundException | com.newsolicitudes.newsolicitudes.exceptions.AnulacionException e) {
            throw e;
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al resolver anulacion de la solicitud ID %s", idSolicitud), e);
        }
    }

    // Anula la solicitud de forma directa por un administrador
    @Override
    @Transactional
    public String anularSolicitudDirecta(Long idSolicitud, String motivo, Integer rutAprobador) {
        try {
            Solicitud solicitud = getSolicitudById(idSolicitud);
            return solicitudAnulacionGestor.anularDirecto(solicitud, motivo, rutAprobador);
        } catch (NotFoundException | com.newsolicitudes.newsolicitudes.exceptions.AnulacionException e) {
            throw e;
        } catch (Exception e) {
            throw new SolicitudException(String.format("Error al procesar la anulacion directa de la solicitud ID %s", idSolicitud), e);
        }
    }

    private Solicitud getSolicitudById(Long idSolicitud) {
        return solicitudRepository
                .findById(idSolicitud)
                .orElseThrow(() -> new NotFoundException("No se encuentra la solicitud"));
    }

}