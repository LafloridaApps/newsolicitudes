package com.newsolicitudes.newsolicitudes.services.ceropapel;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public final class ComparadorPerfilCeroPapel {
    private ComparadorPerfilCeroPapel() {}
    public record Perfil(Long id, String cargo, String unidad) {}
    public record Opciones(String departamento, Long perfilSugerido, List<Perfil> perfiles) {}

    static String normalizar(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\bdirec\\b", "direccion")
                .replaceAll("\\b(de|del|la|las|los|el)\\b", " ").trim().replaceAll(" +", " ");
    }

    public static Long comparar(String departamento, List<Perfil> perfiles) {
        String nombre = normalizar(departamento);
        if (nombre.isBlank()) return null;
        List<Perfil> coincidencias = perfiles.stream().filter(perfil -> {
            String unidad = normalizar(perfil.unidad());
            if (!unidad.isBlank()) return unidad.equals(nombre);
            // Sin unidad, exigir la frase completa y al menos dos palabras significativas.
            return nombre.contains(" ") && (" " + normalizar(perfil.cargo()) + " ").contains(" " + nombre + " ");
        }).toList();
        return coincidencias.size() == 1 ? coincidencias.getFirst().id() : null;
    }
}
