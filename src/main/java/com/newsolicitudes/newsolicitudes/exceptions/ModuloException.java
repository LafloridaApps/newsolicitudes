package com.newsolicitudes.newsolicitudes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ModuloException extends RuntimeException {
    public ModuloException(String message) {
        super(message);
    }
}
