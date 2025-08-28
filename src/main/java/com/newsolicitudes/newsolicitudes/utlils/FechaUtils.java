package com.newsolicitudes.newsolicitudes.utlils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FechaUtils {

    private static final ZoneId SANTIAGO_ZONE = ZoneId.of("America/Santiago");

    private FechaUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static LocalDate fechaActual() {

        ZonedDateTime fechaActualSantiago = ZonedDateTime.now(SANTIAGO_ZONE);
        return fechaActualSantiago.toLocalDate();

    }

    public static LocalDate getFirstDayOfCurrentMonth() {
        YearMonth yearMonth = YearMonth.now(SANTIAGO_ZONE);
        return yearMonth.atDay(1);
    }

    public static LocalDate getLastDayOfCurrentMonth() {
        YearMonth yearMonth = YearMonth.now(SANTIAGO_ZONE);
        return yearMonth.atEndOfMonth();
    }

}
