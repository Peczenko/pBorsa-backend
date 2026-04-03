package com.pborsa.domain.exception;

/**
 * Exception thrown when a user tries to access resources they don't have permission for.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}


