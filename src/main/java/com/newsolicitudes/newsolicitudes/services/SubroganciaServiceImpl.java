package com.newsolicitudes.newsolicitudes.services;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Subrogancia;
import com.newsolicitudes.newsolicitudes.repositories.DepartamentoRepository;
import com.newsolicitudes.newsolicitudes.repositories.FuncionarioRepository;
import com.newsolicitudes.newsolicitudes.repositories.SubroganciaRepository;
import com.newsolicitudes.newsolicitudes.services.interfaces.SubroganciaService;
import com.newsolicitudes.newsolicitudes.utlils.RepositoryUtils;

@Service
public class SubroganciaServiceImpl implements SubroganciaService {

        private final FuncionarioRepository funcionarioRepository;

        private final DepartamentoRepository departamentoRepository;

        private final SubroganciaRepository subroganciaRepository;

        public SubroganciaServiceImpl(FuncionarioRepository funcionarioRepository,
                        DepartamentoRepository departamentoRepository, SubroganciaRepository subroganciaRepository) {
                this.funcionarioRepository = funcionarioRepository;
                this.departamentoRepository = departamentoRepository;
                this.subroganciaRepository = subroganciaRepository;
        }

        @Override
        public Subrogancia createSubrogancia(SubroganciaRequest request) {

                return null;

        }

        private Funcionario getFuncionarioById(Long id) {
                return RepositoryUtils.findOrThrow(funcionarioRepository.findById(id),
                                String.format("Funcionario %d no existe", id));
        }

        private Departamento getDepartamentoById(Long id) {
                return RepositoryUtils.findOrThrow(departamentoRepository.findById(id),
                                String.format("Departamento %d no existe", id));

        }

}
