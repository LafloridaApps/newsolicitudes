package com.newsolicitudes.newsolicitudes.services.subrogancia;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.exceptions.SubroganciaException;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class SubroganciaServiceImpl implements SubroganciaService {

        private final SubroganciaRepository subroganciaRepository;

        public SubroganciaServiceImpl(SubroganciaRepository subroganciaRepository) {
                this.subroganciaRepository = subroganciaRepository;

        }

        @Override
        public void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin,
                        Long idDepto) {

                if (tieneSubroganciaPeriodo(request.getRutJefe(), fechaInicio, fechaFin, idDepto)) {
                        throw new SubroganciaException("Ya existe una subrogancia para este rut en el período dado");
                }

                Subrogancia subrogancia = new Subrogancia(request.getRutSubrogante(), fechaInicio, fechaFin,
                                request.getRutJefe(),
                                idDepto);
                subroganciaRepository.save(subrogancia);

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
