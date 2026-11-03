package com.newsolicitudes.newsolicitudes.services.subrogancia;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.newsolicitudes.newsolicitudes.services.ceropapel.SubroganciaCeroPapelEvent;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.SubroganciaException;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class SubroganciaServiceImpl implements SubroganciaService {

        private final SubroganciaRepository subroganciaRepository;

        private final ApplicationEventPublisher events;

        public SubroganciaServiceImpl(SubroganciaRepository subroganciaRepository, ApplicationEventPublisher events) {
                this.subroganciaRepository = subroganciaRepository;
                this.events = events;

        }

        @Override
        @Transactional
        public void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin,
                        Long idDepto) {

                if (tieneSubroganciaPeriodo(request.getRutJefe(), fechaInicio, fechaFin, idDepto)) {
                        throw new SubroganciaException("Ya existe una subrogancia para este rut en el período dado");
                }

                Subrogancia subrogancia = new Subrogancia(request.getRutSubrogante(), fechaInicio, fechaFin,
                                request.getRutJefe(),
                                idDepto);
                if (request.isGrabarCeroPapel()) {
                        subrogancia.setCeroPapelEstado("PENDIENTE_ENVIO");
                }
                subroganciaRepository.save(subrogancia);
                if (request.isGrabarCeroPapel()) {
                        events.publishEvent(new SubroganciaCeroPapelEvent(subrogancia.getId()));
                }

        }

        private boolean tieneSubroganciaPeriodo(Integer rut, LocalDate fechaInicio, LocalDate fechaFin, Long idDepto) {

                return subroganciaRepository.existeSubrogancia(rut, fechaInicio, fechaFin, idDepto).isPresent();

        }

        @Override
        public void borrarSubrogancia(Long idSubrogancia) {
                Subrogancia subrogancia = RepositoryUtils.findOrThrow(subroganciaRepository.findById(idSubrogancia),
                                String.format("No existe la subrogancia %d", idSubrogancia));

                subroganciaRepository.delete(subrogancia);
        }

}
