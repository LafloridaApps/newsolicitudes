package com.newsolicitudes.newsolicitudes.exceptions;

public class SolicitudException extends RuntimeException {
    public SolicitudException(String message) {
        super(message);
    }

    public SolicitudException(String message, Throwable cause) {
        super(message, cause);
    }
}
