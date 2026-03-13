package com.newsolicitudes.newsolicitudes.services.searchsubrogancia;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SearchSubroganciResponse;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;

@Service
public class SubroganciasSearchImpl implements SubroganciasSearch {

    private final SubroganciaRepository subroganciaRepository;
    private final SearchMapper searchMapper;

    public SubroganciasSearchImpl(SubroganciaRepository subroganciaRepository, SearchMapper searchMapper) {
        this.subroganciaRepository = subroganciaRepository;
        this.searchMapper = searchMapper;
    }

    @Override
    public List<SearchSubroganciResponse> buscarSubrogancia(Integer rutJefe, Integer subrogante, Long idDepto) {

        // build a combined specification; each helper returns a conjunction when
        // the parameter is null, so this works even if all filters are absent.
        Specification<Subrogancia> spec = SubroganciaSpec.hasRutJefe(rutJefe)
                .and(SubroganciaSpec.hasSubrogante(subrogante))
                .and(SubroganciaSpec.hasDepartamento(idDepto));
                

        List<Subrogancia> list = subroganciaRepository.findAll(spec);

        return searchMapper.mapToDto(list);

    }

}
