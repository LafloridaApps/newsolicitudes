package com.newsolicitudes.newsolicitudes.exceptions;

public class DerivacionProcesoException extends RuntimeException {
    public DerivacionProcesoException(String message) {
        super(message);
    }

    public DerivacionProcesoException(String message, Throwable cause) {
        super(message, cause);
    }
}