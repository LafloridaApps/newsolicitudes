package com.newsolicitudes.newsolicitudes.services.interfaces;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;

public interface SubroganciaService {

    Subrogancia createSubrogancia(SubroganciaRequest request);

}
