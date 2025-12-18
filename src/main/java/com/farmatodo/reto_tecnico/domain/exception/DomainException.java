package com.farmatodo.reto_tecnico.domain.exception;

/**
 * Base exception for all domain-related errors.
 * All domain exceptions should extend this class.
 */
public class DomainException extends RuntimeException {

    /**
     * Creates a new DomainException with a message.
     * @param message the error message
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Creates a new DomainException with a message and cause.
     * @param message the error message
     * @param cause the underlying cause
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
