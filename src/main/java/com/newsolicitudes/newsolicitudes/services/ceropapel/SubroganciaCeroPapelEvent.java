package com.newsolicitudes.newsolicitudes.services.ceropapel;

public record SubroganciaCeroPapelEvent(Long subroganciaId, Origen origen) {
    public enum Origen { AUTOMATICO_SOLICITUD, MANUAL, CONFIRMACION_MANUAL }

    public SubroganciaCeroPapelEvent(Long subroganciaId) {
        this(subroganciaId, Origen.MANUAL);
    }
}
