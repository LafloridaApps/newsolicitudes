package com.newsolicitudes.newsolicitudes.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.newsolicitudes.newsolicitudes.entities.Solicitud;

@SpringJUnitConfig(SolicitudAutorTest.Config.class)
@Transactional
class SolicitudAutorTest {
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = SolicitudRepository.class)
    static class Config {
        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:autor;DB_CLOSE_DELAY=-1", "sa", ""));
            factory.setPackagesToScan("com.newsolicitudes.newsolicitudes.entities");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(java.util.Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean
        JpaTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    @Autowired
    private SolicitudRepository repository;

    @Test
    void completaVaciosSinCambiarAutoresRegistradosYEsIdempotente() {
        Solicitud antigua = new Solicitud();
        antigua.setRut(123);
        repository.saveAndFlush(antigua);
        Solicitud tercero = new Solicitud();
        tercero.setRut(456);
        tercero.setRutIngresadoPor(789);
        repository.saveAndFlush(tercero);

        assertEquals(1, repository.completarRutIngresadoPor());
        assertEquals(123, repository.findById(antigua.getId()).orElseThrow().getRutIngresadoPor());
        assertEquals(789, repository.findById(tercero.getId()).orElseThrow().getRutIngresadoPor());
        assertEquals(0, repository.completarRutIngresadoPor());
    }
}
