package com.newsolicitudes.newsolicitudes.services.trazabilidad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.Trazabilidad;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Postergacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.enums.EstadoTrazabilidad;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.PostergacionRepository;
import com.newsolicitudes.newsolicitudes.services.departamento.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;

@Service
public class TrazabilidadServiceImpl implements TrazabilidadService {

    private final PostergacionRepository postergacionRepository;
    private final FuncionarioService funcionarioService;
    private final DepartamentoService departamentoService;
    private final AprobacionRepository aprobacionRepository;

    public TrazabilidadServiceImpl(PostergacionRepository postergacionRepository,
            FuncionarioService funcionarioService, DepartamentoService departamentoService,
            AprobacionRepository aprobacionRepository) {
        this.postergacionRepository = postergacionRepository;
        this.funcionarioService = funcionarioService;
        this.departamentoService = departamentoService;
        this.aprobacionRepository = aprobacionRepository;
    }

    @Override
    public List<Trazabilidad> construirTrazabilidad(Solicitud solicitud) {
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
        return trazabilidadList;
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
}
