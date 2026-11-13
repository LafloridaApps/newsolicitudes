package com.newsolicitudes.newsolicitudes.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.newsolicitudes.newsolicitudes.entities.Modulo;
import com.newsolicitudes.newsolicitudes.repositories.ModuloRepository;
import com.newsolicitudes.newsolicitudes.services.ceropapel.ConfiguracionCeroPapelService;

@Configuration
public class CeroPapelConfiguracionInicial {
    @Bean
    ApplicationRunner registrarConfiguracionCeroPapel(ConfiguracionCeroPapelService service, ModuloRepository modulos) {
        return args -> {
            if (modulos.findByNombre(ConfiguracionCeroPapelService.MODULO).isEmpty()) {
                modulos.save(new Modulo(ConfiguracionCeroPapelService.MODULO));
            }
            service.inicializar();
        };
    }
}
