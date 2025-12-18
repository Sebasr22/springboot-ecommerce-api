package com.farmatodo.reto_tecnico.domain.exception;

/**
 * Exception thrown when credit card tokenization fails.
 */
public class TokenizationFailedException extends DomainException {

    /**
     * Creates a new TokenizationFailedException.
     * @param message the error message
     */
    public TokenizationFailedException(String message) {
        super(message);
    }

    /**
     * Creates a new TokenizationFailedException with cause.
     * @param message the error message
     * @param cause the underlying cause
     */
    public TokenizationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
