package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionCreateService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.VisacionService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class DerivacionCreateServiceImpl implements DerivacionCreateService {

    private final DerivacionService derivacionService;

    private final DerivacionRepository derivacionRepository;

    private final VisacionService visacionService;

    private final ApiDepartamentoService apiDepartamentoService;

    public DerivacionCreateServiceImpl(DerivacionService derivacionService, DerivacionRepository derivacionRepository,
            VisacionService visacionService, ApiDepartamentoService apiDepartamentoService) {
        this.derivacionService = derivacionService;
        this.derivacionRepository = derivacionRepository;
        this.visacionService = visacionService;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    public void createDerivacionDepto(Long idDerivacion) {

        Derivacion derivacion = getDerivacionBydId(idDerivacion);

        Solicitud solicitud = derivacion.getSolicitud();

        Long departamento = derivacion.getIdDepto();

        DepartamentoResponse deptoResponse = apiDepartamentoService.obtenerDepartamento(departamento);

        Long departamentoSiguiente = deptoResponse.getIdDeptoSuperior();

        NivelDepartamento nivelDepartamento = NivelDepartamento.valueOf(deptoResponse.getNivelDepartamento());

        TipoDerivacion tipoDerivacion = switch (nivelDepartamento) {
            case ALCALDIA, ADMINISTRACION, SUBDIRECCION, DIRECCION -> TipoDerivacion.FIRMA;
            default -> TipoDerivacion.VISACION;
        };

        EstadoDerivacion estadoDerivacion = estadoDerivacion(derivacion);
        if (estadoDerivacion == EstadoDerivacion.DERIVADA
                || nivelDepartamento == NivelDepartamento.DEPARTAMENTO
                || nivelDepartamento == NivelDepartamento.SECCION
                || nivelDepartamento == NivelDepartamento.OFICINA) {

            visacionService.visarSolicitud(solicitud, deptoResponse.getRutJefeSuperior());
        }
        derivacion.setEstadoDerivacion(estadoDerivacion);
        derivacionRepository.save(derivacion);

        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamentoSiguiente,
                EstadoDerivacion.PENDIENTE);

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
