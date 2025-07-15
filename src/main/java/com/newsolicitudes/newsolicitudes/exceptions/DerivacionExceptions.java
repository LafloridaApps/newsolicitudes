package com.newsolicitudes.newsolicitudes.exceptions;

public class DerivacionExceptions extends RuntimeException {
    public DerivacionExceptions(String message) {
        super(message);
    }

    public DerivacionExceptions(String msg, Throwable cause) {
        super(msg, cause);
    }
}
