package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for audit logs.
 * Stores business events and transactions for traceability and compliance.
 *
 * Purpose:
 * - RF8: Centralized logging of all business operations
 * - Track complete transaction flows using trace ID
 * - Audit trail for payments, orders, and errors
 * - Debugging and monitoring in production
 *
 * Indexed columns:
 * - trace_id: Query all events in a single HTTP request
 * - event_timestamp: Time-based queries and analytics
 * - entity_type + entity_id: Query all events for a specific entity
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_log_trace_id", columnList = "trace_id"),
        @Index(name = "idx_audit_log_timestamp", columnList = "event_timestamp"),
        @Index(name = "idx_audit_log_entity", columnList = "entity_type, entity_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    /**
     * Unique identifier for the audit log entry.
     */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Trace ID from HTTP request (MDC).
     * Used to correlate all events in a single transaction.
     *
     * Example: All payment retries for an order share the same trace ID.
     */
    @Column(name = "trace_id", length = 36, nullable = false)
    private String traceId;

    /**
     * Type of event being logged.
     *
     * Examples:
     * - PAYMENT_ATTEMPT
     * - PAYMENT_SUCCESS
     * - PAYMENT_FAILED
     * - ORDER_CREATED
     * - ORDER_CANCELLED
     * - STOCK_REDUCED
     * - EMAIL_SENT
     * - ERROR_OCCURRED
     */
    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    /**
     * Type of entity involved in the event.
     *
     * Examples: Order, Payment, Product, Customer, Email
     */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    /**
     * ID of the entity involved in the event.
     *
     * Examples: order ID, payment ID, product ID
     */
    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Status of the event.
     *
     * Examples: SUCCESS, FAILURE, PENDING, IN_PROGRESS
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * Error message if the event failed.
     * NULL if event was successful.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Additional context as JSON string.
     * Optional field for storing extra metadata.
     *
     * Examples:
     * - {"attemptNumber": 2, "maxRetries": 3}
     * - {"oldStock": 10, "newStock": 8}
     */
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;

    /**
     * Timestamp when the event occurred.
     */
    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    /**
     * User ID who triggered the event (if applicable).
     * NULL for system-generated events.
     */
    @Column(name = "user_id")
    private UUID userId;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
    }
}
