package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsolicitudes.newsolicitudes.entities.Derivacion;
import com.newsolicitudes.newsolicitudes.entities.Postergacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.entities.Derivacion.EstadoDerivacion;
import com.newsolicitudes.newsolicitudes.entities.Solicitud.EstadoSolicitud;
import com.newsolicitudes.newsolicitudes.repositories.DerivacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.PostergacionRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.PostergacionService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class PostergacionServiceImpl implements PostergacionService {

    private final PostergacionRepository postergacionRepository;

    private final SolicitudRepository solicitudRepository;

    private final DerivacionRepository derivacionRepository;

    public PostergacionServiceImpl(PostergacionRepository postergacionRepository,
            SolicitudRepository solicitudRepository,
            DerivacionRepository derivacionRepository) {
        this.postergacionRepository = postergacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.derivacionRepository = derivacionRepository;
    }

    @Override
    @Transactional
    public void createPostergacion(Long idSolicitud, String motivo, Integer postergadoPor) {

        Solicitud solicitud = RepositoryUtils.findOrThrow(solicitudRepository.findById(idSolicitud),
                String.format("No se encuentra la solicitud %d", idSolicitud));

        Derivacion derivacion = solicitud.getDerivaciones().stream()
                .filter(d -> d.getEstadoDerivacion().equals(Derivacion.EstadoDerivacion.PENDIENTE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encuentra una derivación aprobada"));

        derivacion.setEstadoDerivacion(EstadoDerivacion.POSTERGADA);

        derivacionRepository.save(derivacion);


                
        solicitud.setEstado(EstadoSolicitud.POSTERGADA);

        Postergacion postergacion = new Postergacion();
        postergacion.setSolicitud(solicitud);
        postergacion.setFechaPostergacion(FechaUtils.fechaActual());
        postergacion.setGlosa(motivo);
        postergacion.setRutPostergacion(postergadoPor);

        postergacionRepository.save(postergacion);
        solicitudRepository.save(solicitud);

    }

}
