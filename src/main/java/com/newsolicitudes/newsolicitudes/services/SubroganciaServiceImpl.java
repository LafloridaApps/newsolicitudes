package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;

@Service
public class SubroganciaServiceImpl implements SubroganciaService {

        private final SubroganciaRepository subroganciaRepository;

        public SubroganciaServiceImpl(SubroganciaRepository subroganciaRepository) {
                this.subroganciaRepository = subroganciaRepository;

        }

        @Override
        public void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin,
                        Long idDepto) {

                Subrogancia subrogancia = new Subrogancia(request.getRutSubrogante(), fechaInicio, fechaFin,
                                request.getRutJefe(),
                                idDepto);
                subroganciaRepository.save(subrogancia);

        }

}
