package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.AprobacionRequest;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.EntradaDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.exceptions.AprobacionException;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.repositories.VisacionRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.AprobacionService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class AprobacionServiceImpl implements AprobacionService {

    private final AprobacionRepository aprobacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final DerivacionRepository derivacionRepository;
    private final VisacionRepository visacionRepository;

    public AprobacionServiceImpl(
            AprobacionRepository aprobacionRepository,
            SolicitudRepository solicitudRepository,
            DerivacionRepository derivacionRepository,
            VisacionRepository visacionRepository) {
        this.aprobacionRepository = aprobacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.derivacionRepository = derivacionRepository;
        this.visacionRepository = visacionRepository;
    }

    @Override
    public void aprobarSolicitud(AprobacionRequest request) {

        Derivacion derivacion = getDerivacionById(request.getIdDerivacion());

        derivacion.setEstadoDerivacion(EstadoDerivacion.FINALIZADA);
        derivacionRepository.save(derivacion);

        Solicitud solicitud = derivacion.getSolicitud();
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitudRepository.save(solicitud);

        // ✅ Obtener funcionario desde la entrada de la derivación
        EntradaDerivacion entrada = derivacion.getEntrada();
        if (entrada == null || entrada.getFuncionario() == null) {
            throw new AprobacionException("No se puede aprobar: la derivación no ha sido recepcionada por un funcionario.");
        }

        Funcionario funcionario = entrada.getFuncionario();

        if (!verificaVisacion(solicitud)) {
            throw new AprobacionException("El funcionario " + funcionario.getNombre()
                    + " no tiene visación para la solicitud " + solicitud.getId());
        }

        Aprobacion aprobacion = crearAprobacion(solicitud, funcionario);
        aprobacionRepository.save(aprobacion);
    }

    private boolean verificaVisacion(Solicitud solicitud) {
        return visacionRepository.existsBySolicitud(solicitud);
    }

    private Aprobacion crearAprobacion(Solicitud solicitud, Funcionario funcionario) {
        Aprobacion aprobacion = new Aprobacion();
        aprobacion.setFechaAprobacion(LocalDate.now());
        aprobacion.setSolicitud(solicitud);
        aprobacion.setFuncionario(funcionario);
        return aprobacion;
    }

    private Derivacion getDerivacionById(Long id) {
        return RepositoryUtils.findOrThrow(derivacionRepository.findById(id),
                String.format("No existe la derivación %d", id));
    }
}
