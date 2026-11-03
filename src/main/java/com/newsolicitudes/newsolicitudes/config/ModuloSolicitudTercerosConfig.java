package com.newsolicitudes.newsolicitudes.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.repositories.ModuloRepository;

@Configuration
public class ModuloSolicitudTercerosConfig {
    @Bean
    ApplicationRunner completarAutorSolicitudes(
            com.newsolicitudes.newsolicitudes.repositories.SolicitudRepository repository) {
        return args -> {
            int actualizadas = repository.completarRutIngresadoPor();
            org.slf4j.LoggerFactory.getLogger(ModuloSolicitudTercerosConfig.class)
                    .info("Solicitudes con RUT de ingreso completado: {}", actualizadas);
        };
    }

    @Bean
    ApplicationRunner registrarModuloSolicitudTerceros(ModuloRepository repository) {
        return args -> {
            if (repository.findByNombre("SOLICITUD POR TERCEROS").isEmpty()) {
                repository.save(new Modulo("SOLICITUD POR TERCEROS"));
            }
        };
    }
}
