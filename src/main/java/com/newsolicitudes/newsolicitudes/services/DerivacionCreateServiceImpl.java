package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.NivelDepartamento;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.TipoDerivacion;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionCreateService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DerivacionService;
import com.newsolicitudes.newsolicitudes.services.interfaces.VisacionService;
import com.newsolicitudes.newsolicitudes.utlils.DepartamentoUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class DerivacionCreateServiceImpl implements DerivacionCreateService {

    private final DerivacionService derivacionService;

    private final DerivacionRepository derivacionRepository;

    private final VisacionService visacionService;

    private final DepartamentoService departamentoService;

    public DerivacionCreateServiceImpl(DerivacionService derivacionService, DerivacionRepository derivacionRepository,
            VisacionService visacionService, DepartamentoService departamentoService) {
        this.derivacionService = derivacionService;
        this.derivacionRepository = derivacionRepository;
        this.visacionService = visacionService;
        this.departamentoService = departamentoService;
    }

    @Override
    public void createDerivacionDepto(Long idDerivacion, Integer rut) {

        Derivacion derivacion = getDerivacionBydId(idDerivacion);

        Solicitud solicitud = derivacion.getSolicitud();

        Long departamento = derivacion.getIdDepto();

        DepartamentoResponse departamentoActual = departamentoService.getDepartamentoById(departamento);

        DepartamentoResponse departamentoSiguiente = departamentoService.getDepartamentoDestino(
                departamentoActual.getRutJefe(),
                departamentoActual);

        Long departamentoSiguienteId = departamentoSiguiente.getId();

        NivelDepartamento nivelDepartamento = DepartamentoUtils.getNivelDepartamento(departamentoSiguiente);
        TipoDerivacion tipoDerivacion = DepartamentoUtils.tipoPorNivel(nivelDepartamento);

      
        EstadoDerivacion estadoDerivacion = estadoDerivacion(derivacion);
        if (estadoDerivacion == EstadoDerivacion.DERIVADA
                || nivelDepartamento == NivelDepartamento.DEPARTAMENTO
                || nivelDepartamento == NivelDepartamento.SECCION
                || nivelDepartamento == NivelDepartamento.OFICINA) {

            visacionService.visarSolicitud(solicitud, departamentoActual.getRutJefeSuperior());
        }
        derivacion.setEstadoDerivacion(estadoDerivacion);
        derivacionRepository.save(derivacion);

        derivacionService.createSolicitudDerivacion(solicitud, tipoDerivacion, departamentoSiguienteId,
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
