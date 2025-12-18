package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when an order is not found.
 */
public class OrderNotFoundException extends DomainException {

    private final UUID orderId;

    /**
     * Creates a new OrderNotFoundException.
     * @param orderId the order ID that was not found
     */
    public OrderNotFoundException(UUID orderId) {
        super(String.format("Order not found with ID: %s", orderId));
        this.orderId = orderId;
    }

    /**
     * Creates a new OrderNotFoundException with custom message.
     * @param message the error message
     */
    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
