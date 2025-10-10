package com.newsolicitudes.newsolicitudes.exceptions;

public class MailServiceException extends RuntimeException {
    public MailServiceException(String message) {
        super(message);
    }

    public MailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}