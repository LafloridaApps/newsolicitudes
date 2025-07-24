package com.newsolicitudes.newsolicitudes.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.SubroganciaRequest;
import com.newsolicitudes.newsolicitudes.entities.Departamento;
import com.newsolicitudes.newsolicitudes.entities.Funcionario;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
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
        public void createSubrogancia(SubroganciaRequest request, LocalDate fechaInicio, LocalDate fechaFin,
                        Long idDepto) {

                Departamento departamentAsubrogar = getDepartamentoById(idDepto);

                Funcionario jefeDepto = getFuncionarioByRut(request.getRutJefe());

                Funcionario subrogado = getFuncionarioByRut(request.getRutSubrogante());

                Subrogancia subrogancia = new Subrogancia(subrogado, fechaInicio, fechaFin, jefeDepto,
                                departamentAsubrogar);
                subroganciaRepository.save(subrogancia);

        }

        private Funcionario getFuncionarioByRut(Integer rut) {
                return RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                                String.format("Funcionario %d no existe", rut));
        }

        private Departamento getDepartamentoById(Long id) {
                return RepositoryUtils.findOrThrow(departamentoRepository.findById(id),
                                String.format("Departamento %d no existe", id));

        }

        @Override
        public boolean estaSubrogandoNivelSuperior(Departamento departamento, Solicitud solicitud) {
                if (departamento == null || departamento.getJefe() == null) {
                        return false;
                }

                List<Subrogancia> subrogancias = subroganciaRepository.findByDepartamento(departamento);
                if (subrogancias.isEmpty()) {

                        subrogancias = subroganciaRepository.findBySubrogante(departamento.getJefe());
                }

                for (Subrogancia sub : subrogancias) {
                        Departamento deptSubrogante = sub.getDepartamento();
                        if (subroganciaVigente(sub, solicitud)
                                        && subrogaNivelConFirma(deptSubrogante, departamento)) {
                                return true;
                        }
                }

                return false;
        }

        private boolean subroganciaVigente(Subrogancia sub, Solicitud solicitud) {
                LocalDate inicioSolicitud = solicitud.getFechaInicio();

                return (inicioSolicitud.isEqual(sub.getFechaInicio()) || inicioSolicitud.isAfter(sub.getFechaInicio()))
                                && (inicioSolicitud.isEqual(sub.getFechaFin())
                                                || inicioSolicitud.isBefore(sub.getFechaFin()));
        }

        private boolean subrogaNivelConFirma(Departamento subrogado, Departamento actual) {
                if (subrogado == null || actual == null) {
                        return false;
                }

                Departamento.NivelDepartamento nivelSubrogado = subrogado.getNivel();
                Departamento.NivelDepartamento nivelActual = actual.getNivel();

                boolean esNivelSuperior = nivelSubrogado.ordinal() < nivelActual.ordinal();
                boolean puedeFirmar = switch (nivelSubrogado) {
                        case DIRECCION, SUBDIRECCION, ADMINISTRACION, ALCALDIA -> true;
                        default -> false;
                };

                return esNivelSuperior && puedeFirmar;
        }

}
