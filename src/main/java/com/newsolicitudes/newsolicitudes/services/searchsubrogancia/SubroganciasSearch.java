package com.newsolicitudes.newsolicitudes.services.searchsubrogancia;

import java.util.List;

import com.newsolicitudes.newsolicitudes.dto.SearchSubroganciResponse;

public interface SubroganciasSearch {

    List<SearchSubroganciResponse> buscarSubrogancia(Integer rutJefe, Integer subrogante, Long idDepto);

}
