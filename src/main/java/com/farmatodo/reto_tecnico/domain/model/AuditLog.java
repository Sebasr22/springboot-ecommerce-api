package com.farmatodo.reto_tecnico.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLog domain entity.
 * Represents a business event or transaction for audit trail purposes.
 * Pure domain model without persistence annotations.
 *
 * Used for:
 * - RF8: Centralized logging requirement
 * - Tracking payment attempts and results
 * - Monitoring order lifecycle
 * - Debugging production issues using trace IDs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /**
     * Unique identifier for the audit log entry.
     */
    private UUID id;

    /**
     * Trace ID from HTTP request.
     * Links all events in a single transaction.
     */
    private String traceId;

    /**
     * Type of event being logged.
     * Examples: PAYMENT_ATTEMPT, ORDER_CREATED, EMAIL_SENT
     */
    private String eventType;

    /**
     * Type of entity involved.
     * Examples: Order, Payment, Product
     */
    private String entityType;

    /**
     * ID of the entity involved.
     */
    private UUID entityId;

    /**
     * Status of the event.
     * Examples: SUCCESS, FAILURE, PENDING
     */
    private String status;

    /**
     * Error message if event failed.
     */
    private String errorMessage;

    /**
     * Additional context as JSON string.
     */
    private String eventData;

    /**
     * Timestamp when event occurred.
     */
    private LocalDateTime eventTimestamp;

    /**
     * User ID who triggered the event (optional).
     */
    private UUID userId;

    /**
     * Creates an audit log for a successful event.
     */
    public static AuditLog success(
            String traceId,
            String eventType,
            String entityType,
            UUID entityId
    ) {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .traceId(traceId)
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .status("SUCCESS")
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an audit log for a failed event.
     */
    public static AuditLog failure(
            String traceId,
            String eventType,
            String entityType,
            UUID entityId,
            String errorMessage
    ) {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .traceId(traceId)
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .status("FAILURE")
                .errorMessage(errorMessage)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an audit log with custom status.
     */
    public static AuditLog create(
            String traceId,
            String eventType,
            String entityType,
            UUID entityId,
            String status
    ) {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .traceId(traceId)
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .status(status)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
