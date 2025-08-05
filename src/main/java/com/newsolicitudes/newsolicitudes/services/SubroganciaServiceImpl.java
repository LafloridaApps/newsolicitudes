package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;

@Service
public class SubroganciaServiceImpl implements SubroganciaService {

        private final SubroganciaRepository subroganciaRepository;

        private final ApiDepartamentoService apiDepartamentoService;

        public SubroganciaServiceImpl(SubroganciaRepository subroganciaRepository,
                        ApiDepartamentoService apiDepartamentoService) {
                this.subroganciaRepository = subroganciaRepository;

                this.apiDepartamentoService = apiDepartamentoService;
        }

        @Override
        public void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin,
                        Long idDepto) {

                DepartamentoResponse departamentAsubrogar = apiDepartamentoService.obtenerDepartamento(idDepto);

                Integer jefeDepto = departamentAsubrogar.getRutJefeSuperior();

                Subrogancia subrogancia = new Subrogancia(request.getRutJefe(), fechaInicio, fechaFin, jefeDepto,
                                idDepto);
                subroganciaRepository.save(subrogancia);

        }

}
