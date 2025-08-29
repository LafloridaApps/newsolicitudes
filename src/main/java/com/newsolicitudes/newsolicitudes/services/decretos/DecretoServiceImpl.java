package com.newsolicitudes.newsolicitudes.services.decretos;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsolicitudes.newsolicitudes.entities.Decreto;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.DecretoRepository;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

@Service
public class DecretoServiceImpl implements DecretoService {

    private final SolicitudRepository solicitudRepository;

    private final DecretoRepository decretoRepository;

    public DecretoServiceImpl(SolicitudRepository solicitudRepository, DecretoRepository decretoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.decretoRepository = decretoRepository;
    }

    @Override
    @Transactional
    public Decreto decretar(Set<Long> ids, Integer rut) {

        List<Solicitud> solicitudes = solicitudRepository.findAllById(ids);

        Decreto nuevoDecreto = new Decreto();
        nuevoDecreto.setRealizadoPor(rut);
        nuevoDecreto.setFechaDecreto(LocalDate.now());
        nuevoDecreto.setFechaHoraTransaccion(FechaUtils.getCurrentDateTime());

        Decreto decretoGuardado = decretoRepository.save(nuevoDecreto);

        for (Solicitud solicitud : solicitudes) {
            solicitud.setEstado(Solicitud.EstadoSolicitud.DECRETADA);
            solicitud.setDecreto(decretoGuardado);
        }

        solicitudRepository.saveAll(solicitudes);

        return decretoGuardado;
    }

}
