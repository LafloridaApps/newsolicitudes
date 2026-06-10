package com.newsolicitudes.newsolicitudes.exceptions;

public class AnulacionException extends RuntimeException {

    public AnulacionException(String message) {
        super(message);
    }

    public AnulacionException(String message, Throwable cause) {
        super(message, cause);
    }


}
