package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.DepartamentoResponse;
import com.newsolicitudes.newsolicitudes.services.interfaces.ApiDepartamentoService;
import com.newsolicitudes.newsolicitudes.services.interfaces.DepartamentoService;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private final ApiDepartamentoService apiDepartamentoService;

    public DepartamentoServiceImpl(ApiDepartamentoService apiDepartamentoService) {
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    public DepartamentoResponse getDepartamentoDestino(Integer rutJefe, DepartamentoResponse departamento) {

        if (rutJefe.equals(departamento.getRutJefe())) {
            return apiDepartamentoService.obtenerDepartamento(departamento.getIdDeptoSuperior());
        }

        if (departamento.getRutJefeSuperior() == null) {
            DepartamentoResponse departamentoSuperior = departamento;
            while (departamentoSuperior.getRutJefe() != null) {
                departamentoSuperior = apiDepartamentoService
                        .obtenerDepartamento(departamentoSuperior.getIdDeptoSuperior());
            }
            return departamentoSuperior;

        }

        return departamento;
    }

    @Override
    public DepartamentoResponse getDepartamentoById(Long id) {
        return apiDepartamentoService.obtenerDepartamento(id);
    }

}
