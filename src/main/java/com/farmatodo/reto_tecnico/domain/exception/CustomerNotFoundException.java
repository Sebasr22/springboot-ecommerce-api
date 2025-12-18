package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a customer is not found.
 */
public class CustomerNotFoundException extends DomainException {

    private final UUID customerId;

    /**
     * Creates a new CustomerNotFoundException.
     * @param customerId the customer ID that was not found
     */
    public CustomerNotFoundException(UUID customerId) {
        super(String.format("Customer not found with ID: %s", customerId));
        this.customerId = customerId;
    }

    /**
     * Creates a new CustomerNotFoundException with custom message.
     * @param message the error message
     */
    public CustomerNotFoundException(String message) {
        super(message);
        this.customerId = null;
    }

    public UUID getCustomerId() {
        return customerId;
    }
}
