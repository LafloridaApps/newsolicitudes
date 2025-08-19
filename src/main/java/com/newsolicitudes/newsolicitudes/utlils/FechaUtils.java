package com.newsolicitudes.newsolicitudes.utlils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FechaUtils {

    private FechaUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static LocalDate fechaActual() {

        ZoneId zonaSantiago = ZoneId.of("America/Santiago");
        ZonedDateTime fechaActualSantiago = ZonedDateTime.now(zonaSantiago);
        return fechaActualSantiago.toLocalDate();

    }

}
