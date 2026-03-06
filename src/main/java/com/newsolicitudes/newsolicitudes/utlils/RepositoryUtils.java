
package com.newsolicitudes.newsolicitudes.utlils;

import java.util.Optional;

import com.newsolicitudes.newsolicitudes.exceptions.NotFoundException;

public class RepositoryUtils {

    private RepositoryUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T findOrThrow(Optional<T> optional, String mensajeError) {
        return optional.orElseThrow(() -> new NotFoundException(mensajeError));
    }

}
