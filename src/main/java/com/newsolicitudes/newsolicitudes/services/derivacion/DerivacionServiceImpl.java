package com.newsolicitudes.newsolicitudes.services.derivacion;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.dto.PageSolicitudesResponse;
import com.newsolicitudes.newsolicitudes.dto.SolicitudDto;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.DerivacionExceptions;
import com.newsolicitudes.newsolicitudes.mappers.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.EntradaDerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.notificacion.NotificacionService;
import com.newsolicitudes.newsolicitudes.services.visacion.VisacionService;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DerivacionServiceImpl implements DerivacionService {

    private static final Logger logger = LoggerFactory.getLogger(DerivacionServiceImpl.class);

    private final DerivacionRepository derivacionRepository;
    private final EntradaDerivacionRepository entradaDerivacionRepository;
    private final SolicitudMapper solicitudMapper;
    private final SubroganciaRepository subroganciaRepository;
    private final DepartamentoService departamentoService;
    private final FuncionarioService funcionarioService;
    private final NotificacionService notificacionService;
    private final AprobacionRepository aprobacionRepository;
    private final VisacionService visacionService;

    public DerivacionServiceImpl(
            DerivacionRepository derivacionRepository,
            EntradaDerivacionRepository entradaDerivacionRepository,
            SolicitudMapper solicitudDtoMapper,
            SubroganciaRepository subroganciaRepository,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            NotificacionService notificacionService,
            AprobacionRepository aprobacionRepository,
            VisacionService visacionService) {
        this.derivacionRepository = derivacionRepository;
        this.entradaDerivacionRepository = entradaDerivacionRepository;
        this.solicitudMapper = solicitudDtoMapper;
        this.subroganciaRepository = subroganciaRepository;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.notificacionService = notificacionService;
        this.aprobacionRepository = aprobacionRepository;
        this.visacionService = visacionService;
    }

    // Crea la derivación inicial para una nueva solicitud.
    @Override
    @Transactional(rollbackFor = DerivacionExceptions.class)
    public void createSolicitudDerivacion(Solicitud solicitud, TipoDerivacion tipo,
            Long idDepto, EstadoDerivacion estadoDerivacion)
            throws DerivacionExceptions {
        crearYNotificarNuevaDerivacion(solicitud, tipo, idDepto, estadoDerivacion);
    }

    // Crea la siguiente derivación en la cadena, típicamente después de una visación.
    @Override
    @Transactional(rollbackFor = DerivacionExceptions.class)
    public void crearSiguienteDerivacion(Long idDerivacionAnterior, Integer rutUsuario) {
        Derivacion derivacionAnterior = getDerivacionBydId(idDerivacionAnterior);
        Solicitud solicitud = derivacionAnterior.getSolicitud();
        DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(derivacionAnterior.getIdDepto());

        // Determina el siguiente departamento en la jerarquía.
        DepartamentoResponse departamentoSiguiente = departamentoService.getDepartamentoDestino(
                departamentoActual.getRutJefe(),
                departamentoActual, solicitud.getFechaInicio(), solicitud.getFechaTermino());

        // Determina si la siguiente derivación es para visación o para firma final.
        TipoDerivacion tipoSiguienteDerivacion = determinaTipoDerivacionFinal(departamentoSiguiente, solicitud.getFechaSolicitud());
        logger.info("Tipo de derivacion determinado: {}", tipoSiguienteDerivacion);

        // Actualiza el estado de la derivación anterior.
        EstadoDerivacion estadoAnterior = calcularEstadoDerivacion(derivacionAnterior);
        if (estadoAnterior == EstadoDerivacion.DERIVADA
                || DepartamentoUtils.getNivelDepartamento(departamentoSiguiente) == NivelDepartamento.DEPARTAMENTO
                || DepartamentoUtils.getNivelDepartamento(departamentoSiguiente) == NivelDepartamento.SECCION
                || DepartamentoUtils.getNivelDepartamento(departamentoSiguiente) == NivelDepartamento.OFICINA) {

            visacionService.visarSolicitud(solicitud, departamentoActual.getRutJefeSuperior());
        }
        derivacionAnterior.setEstadoDerivacion(estadoAnterior);
        derivacionRepository.save(derivacionAnterior);

        // Crea y guarda la nueva derivación pendiente.
        crearYNotificarNuevaDerivacion(solicitud, tipoSiguienteDerivacion, departamentoSiguiente.getId(),
                EstadoDerivacion.PENDIENTE);
    }

    // Obtiene una página de solicitudes basadas en las derivaciones de un departamento.
    @Override
    public PageSolicitudesResponse getDerivacionesByDeptoId(Integer rut, Long idDepto, int pageNumber, Boolean noLeidas) {
        // 1. Obtener subrogancias activas para el RUT del usuario.
        List<Subrogancia> subroganciasActivas = getSubroganciasActivasParaRut(rut);

        // 2. Determinar qué departamentos consultar (el propio y los subrogados).
        List<Long> deptoIds = getDeptoIdsIncluyendoSubrogancias(idDepto, subroganciasActivas);

        // 3. Obtener los datos paginados del repositorio.
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("solicitud.id").descending());
        Page<Derivacion> derivacionesPage = fetchPaginaDerivaciones(deptoIds, noLeidas, pageable);

        // 4. Mapear las entidades a DTOs, pasando las subrogancias para enriquecer la información.
        List<SolicitudDto> solicitudesDto = derivacionesPage.getContent().stream()
                .map(derivacion -> mapDerivacionToSolicitudDto(derivacion, subroganciasActivas))
                .sorted(Comparator.comparing(SolicitudDto::getId, Comparator.reverseOrder()))
                .toList();

        // 5. Construir y devolver la respuesta final paginada.
        return toPageSolicitudesResponse(derivacionesPage, solicitudesDto);
    }

    // =====================================================================================
    // MÉTODOS PRIVADOS DE AYUDA
    // =====================================================================================

    // Lógica central para crear una derivación, persistirla y enviar la notificación.
    private void crearYNotificarNuevaDerivacion(Solicitud solicitud, TipoDerivacion tipo,
            Long idDepto, EstadoDerivacion estadoDerivacion) {
        Derivacion derivacion = new Derivacion();
        derivacion.setSolicitud(solicitud);
        derivacion.setIdDepto(idDepto);
        derivacion.setFechaDerivacion(LocalDate.now());
        derivacion.setEstadoDerivacion(estadoDerivacion);
        derivacion.setTipo(tipo);

        derivacionRepository.save(derivacion);
        enviarNotificacionNuevaDerivacion(derivacion);
    }
    
    // Construye la lista de IDs de departamento a consultar, incluyendo el principal y los subrogados.
    private List<Long> getDeptoIdsIncluyendoSubrogancias(Long idDeptoPrincipal, List<Subrogancia> subroganciasActivas) {
        List<Long> deptoIds = new java.util.ArrayList<>();
        deptoIds.add(idDeptoPrincipal);
        if (!subroganciasActivas.isEmpty()) {
            deptoIds.addAll(subroganciasActivas.stream().map(Subrogancia::getIdDepto).toList());
        }
        return deptoIds;
    }

    // Obtiene la página de derivaciones desde el repositorio, aplicando el filtro de "no leídas" si es necesario.
    private Page<Derivacion> fetchPaginaDerivaciones(List<Long> deptoIds, Boolean noLeidas, Pageable pageable) {
        if (Boolean.TRUE.equals(noLeidas)) {
            return derivacionRepository.findUnreadByIdDeptoIn(deptoIds, pageable);
        } else {
            return derivacionRepository.findByIdDeptoIn(deptoIds, pageable);
        }
    }

    // Construye el objeto de respuesta paginada final.
    private PageSolicitudesResponse toPageSolicitudesResponse(Page<Derivacion> page, List<SolicitudDto> content) {
        PageSolicitudesResponse response = new PageSolicitudesResponse();
        response.setSolicitudes(content);
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setCurrentPage(page.getNumber());
        return response;
    }

    // Mapea una Derivacion a un SolicitudDto, enriqueciendo con datos adicionales.
    private SolicitudDto mapDerivacionToSolicitudDto(Derivacion derivacion, List<Subrogancia> subroganciasActivas) {
        Solicitud solicitud = derivacion.getSolicitud();
        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(solicitud.getRut());
        DepartamentoResponse departamento = departamentoService.getDepartamentoById(solicitud.getIdDepto());
        String urlPdf = getUrlPdf(solicitud);

        // 1. Mapeo base de Solicitud a SolicitudDto.
        SolicitudDto dto = solicitudMapper.solicitudToSolicitudDto(solicitud, funcionario, departamento, urlPdf);
        
        // 2. Añadir la información de la derivación específica.
        dto.setDerivaciones(List.of(getDerivacionDto(derivacion)));

        // 3. Añadir información de subrogancia si corresponde a esta derivación.
        enriquecerConSubroganciaInfo(dto, derivacion, subroganciasActivas);
        
        return dto;
    }

    // Añade información de subrogancia a un SolicitudDto si la derivación corresponde a un depto. subrogado.
    private void enriquecerConSubroganciaInfo(SolicitudDto dto, Derivacion derivacion, List<Subrogancia> subroganciasActivas) {
        Optional<Subrogancia> subroganciaOpt = subroganciasActivas.stream()
                .filter(s -> s.getIdDepto().equals(derivacion.getIdDepto()))
                .findFirst();

        if (subroganciaOpt.isPresent()) {
            Subrogancia subrogancia = subroganciaOpt.get();
            DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
            dto.setSubroganciaInfo(List.of(new com.newsolicitudes.newsolicitudes.dto.SubroganciaInfo(
                    deptoSubrogado.getId(), deptoSubrogado.getNombre(), subrogancia.getFechaInicio(),
                    subrogancia.getFechaFin())));
        } else {
            dto.setSubroganciaInfo(java.util.Collections.emptyList());
        }
    }

    // Envía una notificación por correo para una nueva derivación.
    private void enviarNotificacionNuevaDerivacion(Derivacion derivacion) {
        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(derivacion.getSolicitud().getRut());
        DepartamentoResponse deptoDestino = departamentoService.getDepartamentoById(derivacion.getIdDepto());
        Integer rutJefeDestino = deptoDestino.getRutJefe();
        LocalDate hoy = FechaUtils.fechaActual();

        // Determina el destinatario final, considerando subrogancia.
        List<Subrogancia> subrogancias = subroganciaRepository
                .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeDestino, hoy, hoy);
        FuncionarioResponseApi destinatario;
        if (!subrogancias.isEmpty()) {
            destinatario = funcionarioService.getFuncionarioByRut(subrogancias.get(0).getSubrogante());
        } else {
            destinatario = funcionarioService.getFuncionarioByRut(rutJefeDestino);
        }

        DepartamentoResponse deptoOrigen = departamentoService.getDepartamentoById(funcionario.getCodDepto());

        // Prepara y envía el correo.
        String to = destinatario.getEmail();
        String subject = String.format("Nueva Solicitud de %s", funcionario.getNombreCompleto());
        Map<String, Object> body = new HashMap<>();
        body.put("nombreJefe", destinatario.getNombreCompleto());
        body.put("nombre", funcionario.getNombreCompleto());
        body.put("tipoPermiso", derivacion.getSolicitud().getTipoSolicitud().name());
        body.put("departamento", deptoOrigen.getNombre());
        body.put("link", "https://appx.laflorida.cl/login");
        notificacionService.enviarNotificacion(to, subject, "solicitud", body);
    }

    // Determina si la derivación es de tipo VISACION o FIRMA, considerando la jerarquía y subrogancias.
    private TipoDerivacion determinaTipoDerivacionFinal(DepartamentoResponse deptoDestino, LocalDate fechaChequeo) {
        logger.debug("Determinando tipo de derivacion final para deptoDestino: {} (RutJefe: {}), fechaChequeo: {}",
                deptoDestino.getNombre(), deptoDestino.getRutJefe(), fechaChequeo);

        // 1. Determinar el tipo por defecto segun el nivel del depto destino.
        NivelDepartamento nivelDeptoDestino = DepartamentoUtils.getNivelDepartamento(deptoDestino);
        TipoDerivacion tipoFinal = DepartamentoUtils.tipoPorNivel(nivelDeptoDestino);
        logger.debug("Tipo por defecto segun nivel ({}): {}", nivelDeptoDestino, tipoFinal);

        // 2. Verificar si el jefe del depto destino esta subrogando a un director.
        TipoDerivacion tipoPorSubroganciaJefe = getTipoSiJefeEsSubroganteDirector(deptoDestino.getRutJefe(), fechaChequeo);
        if (tipoPorSubroganciaJefe == TipoDerivacion.FIRMA) {
            logger.debug("Tipo determinado por subrogancia (jefe es subrogante de director): FIRMA");
            return TipoDerivacion.FIRMA;
        }

        // 3. Verificar si el jefe del depto destino esta SIENDO subrogado por un director.
        TipoDerivacion tipoPorJefeSubrogado = getTipoSiJefeEstaSiendoSubrogadoPorDirector(deptoDestino.getRutJefe(), fechaChequeo);
        if (tipoPorJefeSubrogado == TipoDerivacion.FIRMA) {
            logger.debug("Tipo determinado por subrogancia (jefe esta siendo subrogado por director): FIRMA");
            return TipoDerivacion.FIRMA;
        }

        logger.debug("Retornando tipo final por defecto: {}", tipoFinal);
        return tipoFinal;
    }

    // Verifica si un jefe es subrogante de un cargo directivo, lo que implicaría firma.
    private TipoDerivacion getTipoSiJefeEsSubroganteDirector(Integer rutJefe, LocalDate fechaChequeo) {
        logger.debug("Verificando si jefe {} es subrogante de director para fecha {}", rutJefe, fechaChequeo);
        List<Subrogancia> subrogancias = subroganciaRepository.findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefe, fechaChequeo, fechaChequeo);
        if (subrogancias.isEmpty()) return null;

        for (Subrogancia subrogancia : subrogancias) {
            DepartamentoResponse deptoSubrogado = departamentoService.getDepartamentoById(subrogancia.getIdDepto());
            if (deptoSubrogado != null) {
                NivelDepartamento nivel = DepartamentoUtils.getNivelDepartamento(deptoSubrogado);
                if (DepartamentoUtils.tipoPorNivel(nivel) == TipoDerivacion.FIRMA) {
                    return TipoDerivacion.FIRMA;
                }
            }
        }
        return null;
    }

    // Verifica si un jefe está siendo subrogado por un directivo, lo que implicaría firma.
    private TipoDerivacion getTipoSiJefeEstaSiendoSubrogadoPorDirector(Integer rutJefeOriginal, LocalDate fechaChequeo) {
        logger.debug("Verificando si jefe {} esta siendo subrogado por director para fecha {}", rutJefeOriginal, fechaChequeo);
        List<Subrogancia> subrogancias = subroganciaRepository.findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeOriginal, fechaChequeo, fechaChequeo);
        if (subrogancias.isEmpty()) return null;
        
        for (Subrogancia subrogancia : subrogancias) {
            FuncionarioResponseApi funcionarioSubrogante = funcionarioService.getFuncionarioByRut(subrogancia.getSubrogante());
            if (funcionarioSubrogante != null) {
                DepartamentoResponse deptoSubrogante = departamentoService.getDepartamentoById(funcionarioSubrogante.getCodDepto());
                if (deptoSubrogante != null) {
                    NivelDepartamento nivelSubrogante = DepartamentoUtils.getNivelDepartamento(deptoSubrogante);
                    if (DepartamentoUtils.tipoPorNivel(nivelSubrogante) == TipoDerivacion.FIRMA) {
                        return TipoDerivacion.FIRMA;
                    }
                }
            }
        }
        return null;
    }

    // Busca una derivación por ID o lanza una excepción si no se encuentra.
    private Derivacion getDerivacionBydId(Long id) {
        return RepositoryUtils.findOrThrow(derivacionRepository.findById(id),
                String.format("No Existe la derivacion %d", id));
    }

    // Calcula el nuevo estado de una derivación después de ser procesada.
    private EstadoDerivacion calcularEstadoDerivacion(Derivacion derivacion) {
        if (derivacion.getEstadoDerivacion() == null || derivacion.getEstadoDerivacion() == EstadoDerivacion.PENDIENTE) {
            return EstadoDerivacion.DERIVADA;
        }
        return EstadoDerivacion.PENDIENTE;
    }

    // Convierte una entidad Derivacion a su DTO.
    private DerivacionDto getDerivacionDto(Derivacion derivacion) {
        DerivacionDto dto = new DerivacionDto();
        dto.setId(derivacion.getId());
        dto.setFechaDerivacion(derivacion.getFechaDerivacion().toString());
        dto.setEstadoDerivacion(derivacion.getEstadoDerivacion().name());
        dto.setRecepcionada(hasEntrada(derivacion));
        dto.setTipoMovimiento(derivacion.getTipo().name());
        return dto;
    }

    // Verifica si una derivación ha sido recepcionada (tiene una entrada).
    private boolean hasEntrada(Derivacion derivacion) {
        return entradaDerivacionRepository.findByDerivacionId(derivacion.getId()).isPresent();
    }

    // Obtiene las subrogancias activas donde un usuario es el subrogante.
    private List<Subrogancia> getSubroganciasActivasParaRut(Integer rut) {
        LocalDate hoy = FechaUtils.fechaActual();
        return subroganciaRepository
                .findBySubroganteAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rut, hoy, hoy);
    }

    // Obtiene la URL del PDF de una solicitud si ya ha sido aprobada.
    private String getUrlPdf(Solicitud solicitud) {
        Optional<Aprobacion> optAprobacion = aprobacionRepository.findBySolicitud(solicitud);
        return optAprobacion.map(Aprobacion::getUrlPdf).orElse(null);
    }
}
