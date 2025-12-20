package com.farmatodo.reto_tecnico.domain.model;

/**
 * Enumeration of all audit event types tracked in the system.
 *
 * This enum provides type safety and eliminates magic strings throughout the codebase.
 * Every significant business event should have a corresponding entry here.
 *
 * Usage: EventType.ORDER_CREATED.name() returns "ORDER_CREATED" as String
 *
 * Categories:
 * - PAYMENT_*: Payment processing events
 * - ORDER_*: Order lifecycle events
 * - STOCK_*: Inventory management events
 * - ERROR_*: System error events
 *
 * Architecture Note: Belongs to domain layer (framework-free).
 * Referenced by application layer (AuditLogService) and infrastructure layer (GlobalExceptionHandler).
 */
public enum EventType {

    // ===========================================
    // Payment Events
    // ===========================================

    /**
     * Payment attempt initiated.
     * Logged BEFORE attempting payment with gateway.
     */
    PAYMENT_ATTEMPT,

    /**
     * Payment successful.
     * Logged AFTER payment confirmed by gateway.
     */
    PAYMENT_SUCCESS,

    /**
     * Payment failed after all retries exhausted.
     * Logged AFTER final retry attempt.
     */
    PAYMENT_FAILED,

    // ===========================================
    // Order Events
    // ===========================================

    /**
     * Order created successfully.
     * Logged AFTER order persisted to database.
     */
    ORDER_CREATED,

    /**
     * Order status changed.
     * Logged when order transitions between states:
     * - PENDING → PAYMENT_PROCESSING
     * - PAYMENT_PROCESSING → PAYMENT_CONFIRMED
     * - PAYMENT_PROCESSING → PAYMENT_FAILED
     * - Any state → CANCELLED
     */
    ORDER_STATUS_CHANGED,

    // ===========================================
    // Stock Events
    // ===========================================

    /**
     * Product stock reduced.
     * Logged when inventory quantity is decreased (e.g., order placed).
     */
    STOCK_REDUCED,

    // ===========================================
    // Error Events
    // ===========================================

    /**
     * System error occurred.
     * Logged for unexpected exceptions (HTTP 500).
     * Used by GlobalExceptionHandler to track unhandled errors.
     */
    ERROR_OCCURRED
}
