package com.newsolicitudes.newsolicitudes.services.resumeninicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.newsolicitudes.newsolicitudes.dto.AdministrativoDto;
import com.newsolicitudes.newsolicitudes.dto.FeriadosLegalesDto;
import com.newsolicitudes.newsolicitudes.dto.FuncionarioResponseApi;
import com.newsolicitudes.newsolicitudes.dto.ResumenFuncInicio;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;
import com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository;
import com.newsolicitudes.newsolicitudes.services.administrativo.AdministrativoService;
import com.newsolicitudes.newsolicitudes.services.feriadoslegales.FeriadosLegalesService;
import com.newsolicitudes.newsolicitudes.services.funcionario.FuncionarioService;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;

@Service
public class ResmuenFuncInicioServiceImpl implements ResmuenFuncInicioService {

        private final FeriadosLegalesService feriadoService;

        private final AdministrativoService administrativoService;

        private final SolicitudRepository solicitudRepository;

        private final FuncionarioService funcionarioService;

        public ResmuenFuncInicioServiceImpl(FeriadosLegalesService feriadoService,
                        AdministrativoService administrativoService,
                        SolicitudRepository solicitudRepository, FuncionarioService funcionarioService) {
                this.feriadoService = feriadoService;
                this.administrativoService = administrativoService;
                this.solicitudRepository = solicitudRepository;
                this.funcionarioService = funcionarioService;
        }

        @Override
        public ResumenFuncInicio getResumen(Integer rut) {

                // Obtener funcionario
                FuncionarioResponseApi funcionario = funcionarioService.getFuncionarioByRut(rut);

                FeriadosLegalesDto feriados = feriadoService.obtenerFeriados(funcionario.getRut(),
                                funcionario.getIdent());
                AdministrativoDto administrativo = administrativoService.getAdministrativoByRutAndIdent(
                                funcionario.getRut(),
                                funcionario.getIdent());

                Solicitud ultimaSolicitud = solicitudRepository.findTopByRutOrderByFechaSolicitudDesc(rut)
                                .orElse(null);

                List<Solicitud> solicitudesMesActual = solicitudRepository
                                .findByRutAndFechaInicioBetween(rut,
                                                FechaUtils.getFirstDayOfCurrentMonth(),
                                                FechaUtils.getLastDayOfCurrentMonth())
                                .orElse(List.of());

                List<ResumenFuncInicio.SolicitudMes> solicitudesMes = getSolicitudesMes(solicitudesMesActual);

                return ResumenFuncInicio.builder()
                                .saldoFeriado(feriados.getDiasPendientes())
                                .saldoAdministrativo(administrativo.getSaldo())
                                .idUltimaSolicitud(ultimaSolicitud != null ? ultimaSolicitud.getId() : null)
                                .estadoUltimaSolicitud(
                                                ultimaSolicitud != null ? ultimaSolicitud.getEstado().name() : null)
                                .solicitudMes(solicitudesMes)
                                .build();
        }

        private List<ResumenFuncInicio.SolicitudMes> getSolicitudesMes(List<Solicitud> solicitudes) {
                if (solicitudes == null || solicitudes.isEmpty()) {
                        return List.of();
                }

                return solicitudes.stream()
                                .map(d -> new ResumenFuncInicio.SolicitudMes(
                                                d.getTipoSolicitud().name(),
                                                d.getEstado().name(),
                                                d.getFechaSolicitud(),
                                                d.getId()))
                                .toList();
        }

}
