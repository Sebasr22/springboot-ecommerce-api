package com.farmatodo.reto_tecnico.domain.exception;

/**
 * Exception thrown when attempting to register a customer that already exists.
 * Typically identified by duplicate email address.
 */
public class CustomerAlreadyExistsException extends DomainException {

    public CustomerAlreadyExistsException(String email) {
        super(String.format("Customer with email '%s' already exists", email));
    }
}
