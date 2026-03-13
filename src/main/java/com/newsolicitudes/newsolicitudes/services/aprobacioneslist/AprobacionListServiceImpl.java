package com.newsolicitudes.newsolicitudes.services.aprobacioneslist;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.AprobacionList;
import com.newsolicitudes.newsolicitudes.entities.Aprobacion;
import com.newsolicitudes.newsolicitudes.repositories.AprobacionRepository;

@Service
public class AprobacionListServiceImpl implements AprobacionListService {

    private final AprobacionRepository aprobacionRepository;

    private final AprobacionMapper aprobacionMapper;

    public AprobacionListServiceImpl(AprobacionRepository aprobacionRepository,
            AprobacionMapper aprobacionMapper) {
        this.aprobacionRepository = aprobacionRepository;
        this.aprobacionMapper = aprobacionMapper;
    }

    @Override
    public List<AprobacionList> getAprobacionList(LocalDate fechaInicio, LocalDate fechaTermino) {

        List<Aprobacion> aprobaciones = aprobacionRepository.findByFechaAprobacionBetween(fechaInicio, fechaTermino);

        return aprobacionMapper.aprobacionListToAprobacion(aprobaciones);

    }

}
