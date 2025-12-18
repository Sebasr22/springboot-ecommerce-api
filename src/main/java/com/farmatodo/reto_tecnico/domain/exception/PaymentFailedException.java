package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a payment operation fails.
 */
public class PaymentFailedException extends DomainException {

    private final UUID orderId;
    private final String reason;
    private final int attemptNumber;

    /**
     * Creates a new PaymentFailedException with order details.
     * @param orderId the order ID
     * @param reason the failure reason
     * @param attemptNumber the payment attempt number
     */
    public PaymentFailedException(UUID orderId, String reason, int attemptNumber) {
        super(String.format(
                "Payment failed for order %s (attempt %d): %s",
                orderId, attemptNumber, reason
        ));
        this.orderId = orderId;
        this.reason = reason;
        this.attemptNumber = attemptNumber;
    }

    /**
     * Creates a new PaymentFailedException with simple message.
     * @param message the error message
     */
    public PaymentFailedException(String message) {
        super(message);
        this.orderId = null;
        this.reason = message;
        this.attemptNumber = 1;
    }

    /**
     * Creates a new PaymentFailedException with message and cause.
     * @param message the error message
     * @param cause the underlying cause
     */
    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
        this.reason = message;
        this.attemptNumber = 1;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }
}
