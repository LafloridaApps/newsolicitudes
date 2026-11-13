package com.newsolicitudes.newsolicitudes.services.ceropapel;

/** Configuración tomada por operación; desactivar impide nuevas invocaciones HTTP. */
public interface CeroPapelConfiguracion {
    Conexion obtenerConexion();
    boolean estaHabilitada();

    record Conexion(boolean habilitada, String url, String usuario, String clave) {
        @Override public String toString() {
            return "Conexion[habilitada=" + habilitada + ", clave=OCULTA]";
        }
    }
}
