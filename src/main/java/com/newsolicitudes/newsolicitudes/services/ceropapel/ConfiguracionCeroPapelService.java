package com.newsolicitudes.newsolicitudes.services.ceropapel;

import java.net.URI;
import com.newsolicitudes.newsolicitudes.utlils.FechaUtils;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.config.CeroPapelProperties;
import com.newsolicitudes.newsolicitudes.dto.ConfiguracionCeroPapelRequest;
import com.newsolicitudes.newsolicitudes.dto.ConfiguracionCeroPapelResponse;
import com.newsolicitudes.newsolicitudes.entities.ConfiguracionCeroPapel;
import com.newsolicitudes.newsolicitudes.repositories.ConfiguracionCeroPapelRepository;

@Service
public class ConfiguracionCeroPapelService implements CeroPapelConfiguracion {
    public static final String MODULO = "CONFIGURACION CERO PAPEL";
    private final ConfiguracionCeroPapelRepository repository;
    private final CeroPapelProperties properties;
    private final CeroPapelClaveService claves;

    public ConfiguracionCeroPapelService(ConfiguracionCeroPapelRepository repository,
            CeroPapelProperties properties, CeroPapelClaveService claves) {
        this.repository = repository;
        this.properties = properties;
        this.claves = claves;
    }

    @Transactional
    public void inicializar() {
        if (repository.existsById(1L)) return;
        var config = new ConfiguracionCeroPapel();
        config.setId(1L);
        config.setHabilitada(properties.isHabilitada() && !properties.getBaseUrl().isBlank() && !properties.getPassword().isEmpty());
        config.setBaseUrl(properties.getBaseUrl().isBlank() ? "" : validarUrl(properties.getBaseUrl()));
        config.setUsuario(properties.getUsuario());
        config.setClaveCifrada(properties.getPassword().isEmpty() ? new byte[0] : claves.cifrar(properties.getPassword()));
        config.setActualizadoEn(FechaUtils.getCurrentDateTime());
        repository.save(config);
    }

    public com.newsolicitudes.newsolicitudes.dto.CeroPapelValoresInicialesResponse valoresIniciales() {
        return new com.newsolicitudes.newsolicitudes.dto.CeroPapelValoresInicialesResponse(
                properties.getBaseUrl(), properties.getUsuario(), !properties.getPassword().isEmpty());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Conexion obtenerConexion() {
        var config = buscar();
        return new Conexion(config.isHabilitada(), config.getBaseUrl(), config.getUsuario(),
                config.isHabilitada() ? claves.descifrar(config.getClaveCifrada()) : "");
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean estaHabilitada() { return buscar().isHabilitada(); }

    @Transactional(readOnly = true)
    public ConfiguracionCeroPapelResponse consultar() { return respuesta(buscar()); }

    @Transactional(readOnly = true)
    public com.newsolicitudes.newsolicitudes.dto.CeroPapelClaveResponse consultarClave() {
        return new com.newsolicitudes.newsolicitudes.dto.CeroPapelClaveResponse(
                claves.descifrar(buscar().getClaveCifrada()));
    }

    @Transactional
    public ConfiguracionCeroPapelResponse guardar(ConfiguracionCeroPapelRequest request) {
        var config = buscar();
        if (!Objects.equals(config.getVersion(), request.version())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La configuración cambió. Actualice antes de guardar.");
        }
        String url = validarUrl(request.url());
        byte[] clave = config.getClaveCifrada();
        if (request.clave() != null && !request.clave().isEmpty()) clave = claves.cifrar(request.clave());
        if (request.habilitada() && clave.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese la clave antes de activar Cero Papel.");
        }
        config.setHabilitada(request.habilitada());
        config.setBaseUrl(url);
        config.setUsuario(request.usuario().trim());
        config.setClaveCifrada(clave);
        config.setActualizadoEn(FechaUtils.getCurrentDateTime());
        var guardada = repository.saveAndFlush(config);
        LoggerFactory.getLogger(getClass()).info("Configuración CeroPapel actualizada habilitada={} version={}",
                guardada.isHabilitada(), guardada.getVersion());
        return respuesta(guardada);
    }

    private String validarUrl(String valor) {
        try {
            URI uri = URI.create(valor.trim());
            boolean protocolo = "https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme());
            boolean host = uri.getHost() != null && Arrays.stream(properties.getHostsPermitidos().split(","))
                    .map(String::trim).anyMatch(h -> h.equalsIgnoreCase(uri.getHost()));
            if (!protocolo || !host || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || uri.getPort() > 0 || uri.getRawPath().contains("..")) {
                throw new IllegalArgumentException();
            }
            String url = uri.toASCIIString();
            int fin = url.length();
            while (fin > 0 && url.charAt(fin - 1) == '/') {
                fin--;
            }
            return url.substring(0, fin) + "/";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese una URL de Cero Papel permitida.");
        }
    }

    private ConfiguracionCeroPapel buscar() {
        return repository.findById(1L).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "La configuración de Cero Papel no está inicializada."));
    }

    private ConfiguracionCeroPapelResponse respuesta(ConfiguracionCeroPapel config) {
        return new ConfiguracionCeroPapelResponse(config.isHabilitada(), config.getBaseUrl(), config.getUsuario(),
                config.getClaveCifrada().length > 0, config.getVersion());
    }
}
