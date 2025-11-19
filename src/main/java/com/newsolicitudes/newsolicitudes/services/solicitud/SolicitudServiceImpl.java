package com.newsolicitudes.newsolicitudes.services.solicitud;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.DerivacionDto;
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
import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Postergacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.enums.EstadoTrazabilidad;
import com.newsolicitudes.newsolicitudes.exceptions.NotFounException;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.FeriadoRepository;
import com.newsolicitudes.newsolicitudes.repositories.PostergacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.mappers.SolicitudMapper;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.derivacion.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.services.mail.ApiMailService;
import com.newsolicitudes.newsolicitudes.services.subrogancia.SubroganciaService;
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
    private final ApiMailService apiMailService;
    private final SubroganciaRepository subroganciaRepository;
    private final FeriadoRepository feriadoRepository;

    public SolicitudServiceImpl(DerivacionService derivacionService, SolicitudRepository solicitudRepository,
            SubroganciaService subroganciaService,
            DepartamentoService departamentoService,
            FuncionarioService funcionarioService,
            SolicitudMapper solicitudMapper,
            AprobacionRepository aprobacionRepository,
            PostergacionRepository postergacionRepository,
            ApiMailService apiMailService,
            SubroganciaRepository subroganciaRepository,
            FeriadoRepository feriadoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.derivacionService = derivacionService;
        this.subroganciaService = subroganciaService;
        this.departamentoService = departamentoService;
        this.funcionarioService = funcionarioService;
        this.solicitudMapper = solicitudMapper;
        this.aprobacionRepository = aprobacionRepository;
        this.postergacionRepository = postergacionRepository;
        this.apiMailService = apiMailService;
        this.subroganciaRepository = subroganciaRepository;
        this.feriadoRepository = feriadoRepository;
    }

    @Override
    @Transactional
    public SolicitudResponse createSolicitud(SolicitudRequest request) {
        FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(request.getRut());
        DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(funcionario.getCodDepto());
        DepartamentoResponse departamentoDestino = departamentoService.getDepartamentoDestino(request.getRut(),
                departamentoActual, request.getFechaInicio(), request.getFechaFin());

        NivelDepartamento nivelDepartamento = DepartamentoUtils.getNivelDepartamento(departamentoDestino);
        TipoDerivacion tipoDerivacion = DepartamentoUtils.tipoPorNivel(nivelDepartamento);

        double cantidadDias = calcularDias(request);
        Solicitud solicitud = solicitudMapper.solicitudRequestToSolicitud(request, funcionario.getRut(),
                funcionario.getCodDepto(), cantidadDias);

        solicitud = solicitudRepository.save(solicitud);
        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamentoDestino.getId(),
                EstadoDerivacion.PENDIENTE);
        if (request.getSubrogancia() != null) {
            createSubroganciaSol(request.getSubrogancia(), request.getFechaInicio(), request.getFechaFin(),
                    request.getDepto());
        }

        sendMailSolicitud(solicitud, departamentoDestino, funcionario, departamentoActual.getNombre());
        return new SolicitudResponse(solicitud.getId(), departamentoDestino.getNombre());
    }

    private void createSubroganciaSol(SubroganciaRequest subrogancia, LocalDate fechaInicio, LocalDate fechaFin,
            Long idDepto) {
        subroganciaService.createSubrogancia(subrogancia, fechaInicio, fechaFin, idDepto);
    }

    private void sendMailSolicitud(Solicitud solicitud, DepartamentoResponse departamentoDestino,
            FuncionarioResponseApi funcionario, String nombreDepartamentoActual) {

        Integer rutJefeDestino = departamentoDestino.getRutJefe();
        LocalDate hoy = LocalDate.now();
        List<Subrogancia> subrogancias = subroganciaRepository
                .findByJefeDepartamentoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(rutJefeDestino, hoy, hoy);

        FuncionarioResponseApi destinatario;
        if (!subrogancias.isEmpty()) {
            destinatario = funcionarioService.getFuncionarioByRut(subrogancias.get(0).getSubrogante());
        } else {
            destinatario = funcionarioService.getFuncionarioByRut(rutJefeDestino);
        }

        String to = destinatario.getEmail();
        String subject = String.format("Nueva Solicitud de %s", funcionario.getNombreCompleto());
        String templateName = "solicitud";

        Map<String, Object> body = new HashMap<>();
        body.put("nombreJefe", destinatario.getNombreCompleto());
        body.put("nombre", funcionario.getNombreCompleto());
        body.put("tipoPermiso", solicitud.getTipoSolicitud().name());
        body.put("departamento", nombreDepartamentoActual);
        body.put("link", "https://appx.laflorida.cl/login");

        apiMailService.enviarMail(to, subject, templateName, body);
    }

    @Override
    public Map<String, Object> existeSolicitudByFechaAndTipo(Integer rut, LocalDate fechaInicio, String tipo) {
        Optional<Solicitud> solicitudOptional = solicitudRepository
                .findFirstByRutAndTipoSolicitudAndFechaInicioLessThanEqualAndFechaTerminoGreaterThanEqual(
                        rut, Solicitud.TipoSolicitud.valueOf(tipo), fechaInicio, fechaInicio);
        Map<String, Object> response = new HashMap<>();

        if (solicitudOptional.isPresent()) {
            Solicitud solicitud = solicitudOptional.get();
            response.put("existe", true);
            response.put("estado", solicitud.getEstado() != null ? solicitud.getEstado().name() : null);
            response.put("fechaInicio",
                    solicitud.getFechaInicio() != null ? solicitud.getFechaInicio().toString() : null);
            response.put("fechaTermino",
                    solicitud.getFechaTermino() != null ? solicitud.getFechaTermino().toString() : null);
            response.put("jornadaInicio",
                    solicitud.getJornadaInicio() != null ? solicitud.getJornadaInicio().name() : null);
            response.put("jornadaTermino",
                    solicitud.getJornadaTermino() != null ? solicitud.getJornadaTermino().name() : null);
        } else {
            response.put("existe", false);
            response.put("estado", null);
            response.put("fechaInicio", null);
            response.put("fechaTermino", null);
            response.put("jornadaInicio", null);
            response.put("jornadaTermino", null);
        }
        return response;
    }

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

    private MiSolicitudDto mapToMiSolicitudDto(Solicitud solicitud) {
        String urlPdf = getUrlPdf(solicitud);

        List<Trazabilidad> trazabilidadList = new ArrayList<>(solicitud.getDerivaciones().stream()
                .map(this::mapToTrazabilidad)
                .toList());

        if (solicitud.getEstado() == Solicitud.EstadoSolicitud.POSTERGADA) {
            Optional<Postergacion> postergacionOpt = postergacionRepository.findBySolicitud(solicitud);
            if (postergacionOpt.isPresent()) {
                Postergacion postergacion = postergacionOpt.get();
                Trazabilidad t = new Trazabilidad();
                t.setAccion("Postergación");
                t.setEstado(EstadoTrazabilidad.POSTERGADA);
                t.setFecha(postergacion.getFechaPostergacion().toString());
                FuncionarioResponseApi fr = funcionarioService.getFuncionarioByRut(postergacion.getRutPostergacion());
                t.setUsuario(fr.getNombre() + " " + fr.getApellidoPaterno());
                DepartamentoResponse dr = departamentoService.getDepartamentoById(fr.getCodDepto());
                t.setDepartamento(dr.getNombre());
                t.setGlosa(postergacion.getGlosa());
                trazabilidadList.add(t);
            }
        }

        return solicitudMapper.solicitudToMiSolicitudDto(solicitud, urlPdf, trazabilidadList);
    }

    private Trazabilidad mapToTrazabilidad(Derivacion derivacion) {
        Trazabilidad trazabilidad = new Trazabilidad();
        trazabilidad.setId(derivacion.getId());
        trazabilidad.setFecha(derivacion.getFechaDerivacion().toString());

        DepartamentoResponse depto = departamentoService.getDepartamentoById(derivacion.getIdDepto());
        trazabilidad.setDepartamento(depto.getNombre());

        if (derivacion.getTipo() == TipoDerivacion.VISACION) {
            handleVisacion(trazabilidad, derivacion);
        } else if (derivacion.getTipo() == TipoDerivacion.FIRMA) {
            handleFirma(trazabilidad, derivacion);
        }

        return trazabilidad;
    }

    private void handleVisacion(Trazabilidad trazabilidad, Derivacion derivacion) {
        trazabilidad.setAccion("Visación");
        EntradaDerivacion entrada = derivacion.getEntrada();
        if (entrada != null) {
            setUsuarioFromEntrada(trazabilidad, entrada);
            if (derivacion.getEstadoDerivacion() == EstadoDerivacion.DERIVADA
                    || derivacion.getEstadoDerivacion() == EstadoDerivacion.FINALIZADA) {
                trazabilidad.setEstado(EstadoTrazabilidad.REALIZADO);
            } else {
                trazabilidad.setEstado(EstadoTrazabilidad.RECIBIDO);
            }
        } else {
            setEstadoPendiente(trazabilidad);
        }
    }

    private void handleFirma(Trazabilidad trazabilidad, Derivacion derivacion) {
        trazabilidad.setAccion("Aprobación");
        Optional<Aprobacion> aprobacionOpt = aprobacionRepository.findBySolicitud(derivacion.getSolicitud());
        if (aprobacionOpt.isPresent()) {
            setUsuarioFromAprobacion(trazabilidad, aprobacionOpt.get());
            trazabilidad.setEstado(EstadoTrazabilidad.REALIZADO);
        } else {
            EntradaDerivacion entrada = derivacion.getEntrada();
            if (entrada != null) {
                setUsuarioFromEntrada(trazabilidad, entrada);
                trazabilidad.setEstado(EstadoTrazabilidad.RECIBIDO);
            } else {
                setEstadoPendiente(trazabilidad);
            }
        }
    }

    private void setUsuarioFromEntrada(Trazabilidad trazabilidad, EntradaDerivacion entrada) {
        FuncionarioResponseApi fr = funcionarioService.getFuncionarioByRut(entrada.getRut());
        trazabilidad.setUsuario(fr.getNombre() + " " + fr.getApellidoPaterno());
    }

    private void setUsuarioFromAprobacion(Trazabilidad trazabilidad, Aprobacion aprobacion) {
        FuncionarioResponseApi fr = funcionarioService.getFuncionarioByRut(aprobacion.getRut());
        trazabilidad.setUsuario(fr.getNombre() + " " + fr.getApellidoPaterno());
    }

    private void setEstadoPendiente(Trazabilidad trazabilidad) {
        trazabilidad.setUsuario("Pendiente");
        trazabilidad.setEstado(EstadoTrazabilidad.PENDIENTE);
    }

    @Override
    public SolicitudDetalleDto getSolicitudDetalleById(Long idSolicitud) {
        Optional<Solicitud> solicitudOpt = solicitudRepository.findById(idSolicitud);
        if (solicitudOpt.isPresent()) {
            Solicitud solicitud = solicitudOpt.get();

            FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(solicitud.getRut());
            String nombreFuncionario = funcionario.getNombreCompleto();

            DepartamentoResponse departamento = departamentoService.getDepartamentoById(solicitud.getIdDepto());
            String nombreDepartamento = departamento.getNombre();

            String urlPdf = getUrlPdf(solicitud);

            List<DerivacionDto> derivaciones = solicitud.getDerivaciones().stream().map(der -> {
                DepartamentoResponse depto = departamentoService.getDepartamentoById(der.getIdDepto());
                return solicitudMapper.derivacionToDerivacionDto(der, depto.getNombre());
            }).toList();

            return solicitudMapper.solicitudToSolicitudDetalleDto(solicitud, nombreFuncionario, nombreDepartamento,
                    urlPdf, derivaciones);
        }
        return null;
    }

    private String getUrlPdf(Solicitud solicitud) {
        Optional<Aprobacion> optAprobacion = aprobacionRepository.findBySolicitud(solicitud);
        return optAprobacion.map(Aprobacion::getUrlPdf).orElse(null);
    }

    private double calcularDias(SolicitudRequest request) {
        String tipo = request.getTipoSolicitud();
        if (tipo.equalsIgnoreCase("ADMINISTRATIVO")) {
            return calcularDiasAdministrativo(request);
        } else if (tipo.equalsIgnoreCase("FERIADO")) {
            return calcularDiasFeriado(request);
        }
        return 0;
    }

    private double calcularDiasAdministrativo(SolicitudRequest request) {
        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();

        if (inicio.equals(fin)) {
            return request.getJornadaInicio().equals(request.getJornadaTermino()) ? 0.5 : 1.0;
        }

        double diasHabiles = contarDiasHabiles(inicio, fin);

        if (request.getJornadaInicio().equalsIgnoreCase(request.getJornadaTermino())) {
            diasHabiles -= 0.5;
        }

        return diasHabiles;
    }

    private double calcularDiasFeriado(SolicitudRequest request) {
        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();
        return contarDiasHabiles(inicio, fin);
    }

    private double contarDiasHabiles(LocalDate inicio, LocalDate fin) {
        double dias = 0;
        LocalDate fecha = inicio;

        while (!fecha.isAfter(fin)) {
            if (esDiaHabil(fecha)) {
                dias++;
            }
            fecha = fecha.plusDays(1);
        }

        return dias;
    }

    private boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);
        boolean esFeriado = feriadoRepository.existsByFecha(fecha);

        return !esFinDeSemana && !esFeriado;
    }

    @Override
    @Transactional
    public void updateSolicitud(Long idSolicitud, UpdateSolicitudRequest request) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new NotFounException("Solicitud no encontrada con id: " + idSolicitud));

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

            double cantidadDias = calcularDias(solicitudRequest);
            solicitud.setCantidadDias(cantidadDias);
        }

        if (request.getEstadoSolicitud() != null) {
            Solicitud.EstadoSolicitud nuevoEstado = Solicitud.EstadoSolicitud.valueOf(request.getEstadoSolicitud());
            if (nuevoEstado != solicitud.getEstado()) {
                if (nuevoEstado == Solicitud.EstadoSolicitud.POSTERGADA) {
                    Aprobacion aprobacion = aprobacionRepository.findBySolicitud(solicitud)
                            .orElseThrow(() -> new NotFounException("No se puede postergar una solicitud que no ha sido aprobada."));

                    Postergacion postergacion = new Postergacion();
                    postergacion.setFechaPostergacion(LocalDate.now());
                    postergacion.setRutPostergacion(aprobacion.getRut());
                    postergacion.setSolicitud(solicitud);
                    postergacion.setGlosa("Postergación a través del mantenedor de solicitudes.");
                    postergacionRepository.save(postergacion);
                }
                solicitud.setEstado(nuevoEstado);
            }
        }

        solicitudRepository.save(solicitud);
    }
}

    