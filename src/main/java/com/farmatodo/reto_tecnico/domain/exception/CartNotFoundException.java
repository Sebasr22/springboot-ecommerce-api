package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a cart is not found.
 */
public class CartNotFoundException extends DomainException {

    public CartNotFoundException(UUID customerId) {
        super("Cart not found for customer: " + customerId);
    }

    public CartNotFoundException(String message) {
        super(message);
    }
}
