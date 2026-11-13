package com.newsolicitudes.newsolicitudes.services.ceropapel;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.newsolicitudes.newsolicitudes.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class CeroPapelPermisoService {
    private final UsuarioRepository usuarios;

    public CeroPapelPermisoService(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    public void exigirAdministrador(HttpServletRequest request) {
        String rutGestor = request.getParameter("rutGestor");
        if (rutGestor == null || !rutGestor.matches("[0-9.]+(?:-[0-9kK])?")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique el RUT del usuario que administra Cero Papel.");
        }
        String rut = rutGestor.replace(".", "").split("-")[0];
        boolean autorizado = usuarios.findByRut(rut).stream().flatMap(u -> u.getModulos().stream())
                .anyMatch(m -> ConfiguracionCeroPapelService.MODULO.equals(m.getNombre()));
        if (!autorizado) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tiene el módulo CONFIGURACION CERO PAPEL.");
    }
}
