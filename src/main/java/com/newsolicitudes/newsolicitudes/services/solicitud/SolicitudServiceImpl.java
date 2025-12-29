package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.config.AppProperties;
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
import com.newsolicitudes.newsolicitudes.exceptions.NotFounException;
import com.newsolicitudes.newsolicitudes.exceptions.SolicitudException;
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
import com.newsolicitudes.newsolicitudes.services.notificacion.NotificacionService;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;
import com.newsolicitudes.newsolicitudes.services.trazabilidad.TrazabilidadService;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;

import jakarta.transaction.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final FuncionarioService funcionarioService;
    private final SolicitudRepository solicitudRepository;
    private final DerivacionService derivacionService;
    private final SubroganciaService subroganciaService;
    private final DepartamentoService departamentoService;
    private final SolicitudMapper solicitudMapper;
    private final AprobacionRepository aprobacionRepository;
    private final PostergacionRepository postergacionRepository;
    private final NotificacionService notificacionService;
    private final SubroganciaRepository subroganciaRepository;
    private final CalculadoraDiasService calculadoraDiasService;
    private final TrazabilidadService trazabilidadService;
    private final AppProperties appProperties;

    public SolicitudServiceImpl(DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            SolicitudMapper solicitudMapper,
            AprobacionRepository aprobacionRepository,
            PostergacionRepository postergacionRepository,
            NotificacionService notificacionService,
            SubroganciaRepository subroganciaRepository,
            CalculadoraDiasService calculadoraDiasService,
            TrazabilidadService trazabilidadService,
            AppProperties appProperties) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.solicitudMapper = solicitudMapper;
        this.aprobacionRepository = aprobacionRepository;
        this.postergacionRepository = postergacionRepository;
        this.notificacionService = notificacionService;
        this.subroganciaRepository = subroganciaRepository;
        this.calculadoraDiasService = calculadoraDiasService;
        this.trazabilidadService = trazabilidadService;
        this.appProperties = appProperties;
    }

    // Record privado para encapsular el resultado de la lógica de enrutamiento.
    private record RutaDerivacion(DepartamentoResponse departamentoDestino, TipoDerivacion tipoDerivacion) {
    }

    // Orquesta el proceso completo de creación de una nueva solicitud.
    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {
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
        if (request.getSubrogancia() != null) {
            createSubroganciaSol(request.getSubrogancia(), request.getFechaInicio(), request.getFechaFin(),
                    request.getDepto());
        }

        // 5. Enviar notificación de la nueva solicitud.
        enviarNotificacionNuevaSolicitud(solicitud, ruta.departamentoDestino(), funcionario,
                departamentoActual.getNombre());

        return new SolicitudResponse(solicitud.getId(), ruta.departamentoDestino().getNombre());
    }

    // Determina el departamento de destino y el tipo de derivación para una
    // solicitud.
    private RutaDerivacion determinarRutaDerivacion(SolicitudRequest request, DepartamentoResponse deptoActual) {
        DepartamentoResponse departamentoDestino = departamentoService.getDepartamentoDestino(request.getRut(),
                deptoActual, request.getFechaInicio(), request.getFechaFin());
        NivelDepartamento nivelDepartamento = DepartamentoUtils.getNivelDepartamento(departamentoDestino);
        TipoDerivacion tipoDerivacion = DepartamentoUtils.tipoPorNivel(nivelDepartamento);
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

    // Orquesta el envío de notificaciones para una nueva solicitud.
    private void enviarNotificacionNuevaSolicitud(Solicitud solicitud, DepartamentoResponse departamentoDestino,
            FuncionarioResponseApi funcionario, String nombreDepartamentoActual) {

        // 1. Determinar el destinatario final (jefe o subrogante).
        FuncionarioResponseApi destinatario = determinarDestinatarioNotificacion(departamentoDestino);

        // 2. Preparar el contenido del correo.
        String to = destinatario.getEmail();
        String subject = String.format("Nueva Solicitud de %s", funcionario.getNombreCompleto());
        Map<String, Object> body = prepararCuerpoNotificacion(destinatario, funcionario, solicitud,
                nombreDepartamentoActual);

        // 3. Enviar la notificación a través del servicio correspondiente.
        notificacionService.enviarNotificacion(to, subject, "solicitud", body);
    }

    // Determina el destinatario final para una notificación, considerando la
    // subrogancia activa.
    private FuncionarioResponseApi determinarDestinatarioNotificacion(DepartamentoResponse departamentoDestino) {
        Integer rutJefeDestino = departamentoDestino.getRutJefe();
        LocalDate hoy = LocalDate.now();
        List<Subrogancia> subrogancias = subroganciaRepository
                .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeDestino, hoy, hoy);

        if (!subrogancias.isEmpty()) {
            // Si hay subrogancia activa, el destinatario es el subrogante.
            return funcionarioService.getFuncionarioByRut(subrogancias.get(0).getSubrogante());
        } else {
            // De lo contrario, el destinatario es el jefe titular del departamento.
            return funcionarioService.getFuncionarioByRut(rutJefeDestino);
        }
    }

    // Prepara el cuerpo del correo con los datos necesarios para la plantilla de
    // notificación.
    private Map<String, Object> prepararCuerpoNotificacion(FuncionarioResponseApi destinatario,
            FuncionarioResponseApi funcionarioSolicitante, Solicitud solicitud, String nombreDepartamentoActual) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombreJefe", destinatario.getNombreCompleto());
        body.put("nombre", funcionarioSolicitante.getNombreCompleto());
        body.put("tipoPermiso", solicitud.getTipoSolicitud().name());
        body.put("departamento", nombreDepartamentoActual);
        body.put("link", appProperties.getLinkUrl());
        return body;
    }

    // Verifica si ya existe una solicitud para un funcionario en un rango de fechas
    // determinado.
    @Override
    public ExisteSolicitudResponseDto existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo) {
        Optional<Solicitud> solicitudOptional = solicitudRepository
                .findFirstByRutAndTipoSolicitudAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
                        rut, Solicitud.TipoSolicitud.valueOf(tipo), fechaInicio, fechaInicio);

        return solicitudOptional
                .map(solicitudMapper::solicitudToExisteSolicitudResponseDto)
                .orElse(new ExisteSolicitudResponseDto(false, null, null, null, null, null));
    }

    // Obtiene una lista paginada de las solicitudes de un funcionario.
    @Override
    public PageMiSolicitudResponse getSolicitudesByRut(Integer rut, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
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
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new NotFounException("Solicitud no encontrada con id: " + idSolicitud));

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
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new NotFounException("Solicitud no encontrada con id: " + idSolicitud));

        actualizarFechasSiEsNecesario(solicitud, request);
        gestionarCambioDeEstado(solicitud, request);

        solicitudRepository.save(solicitud);
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
                            () -> new NotFounException("No se puede postergar una solicitud que no ha sido aprobada."));

            Postergacion postergacion = new Postergacion();
            postergacion.setFechaPostergacion(LocalDate.now());
            postergacion.setRutPostergacion(aprobacion.getRut());
            postergacion.setSolicitud(solicitud);
            postergacion.setGlosa("Postergación a través del mantenedor de solicitudes.");
            postergacionRepository.save(postergacion);
        }
        solicitud.setEstado(nuevoEstado);
    }

    @Override
    public boolean buscarSolicitudesPendientesAprobacion(String tipoSolicitud, Integer rutFuncionario) {

        TipoSolicitud tipoSol = (tipoSolicitud != null) ? TipoSolicitud.valueOf(tipoSolicitud) : null;

        List<Solicitud> solicitudesPendientes = solicitudRepository
                .findByTipoSolicitudAndEstadoAndRut(tipoSol, EstadoSolicitud.PENDIENTE, rutFuncionario);
        return !solicitudesPendientes.isEmpty();
    }
}