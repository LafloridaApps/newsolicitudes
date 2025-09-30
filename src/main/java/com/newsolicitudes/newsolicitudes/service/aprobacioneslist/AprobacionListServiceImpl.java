package com.newsolicitudes.newsolicitudes.service.aprobacioneslist;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.AprobacionListPage;
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
    public AprobacionListPage getAprobacionList(LocalDate fechaInicio, LocalDate fechaTermino, int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, 20);

        Page<Aprobacion> aprobaciones = aprobacionRepository.findByFechaAprobacionBetween(fechaInicio, fechaTermino, pageable);

        return aprobacionMapper.aprobacionListToAprobacion(aprobaciones);

    }

}
