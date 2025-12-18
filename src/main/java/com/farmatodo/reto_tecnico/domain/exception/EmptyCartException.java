package com.farmatodo.reto_tecnico.domain.exception;

/**
 * Exception thrown when attempting to checkout an empty cart.
 */
public class EmptyCartException extends DomainException {

    public EmptyCartException() {
        super("Cannot checkout an empty cart");
    }

    public EmptyCartException(String message) {
        super(message);
    }
}
