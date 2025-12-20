package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.AuditLog;
import com.farmatodo.reto_tecnico.domain.model.EventType;
import com.farmatodo.reto_tecnico.domain.port.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for asynchronous audit logging.
 * Records business events and transactions to database for compliance and debugging.
 *
 * CRITICAL: All methods are @Async to avoid blocking main business flow.
 * Uses MdcTaskDecorator to propagate trace IDs to async threads.
 *
 * Purpose (RF8 - Centralized Logging):
 * - Track all payment attempts and results
 * - Monitor order lifecycle
 * - Audit trail for compliance
 * - Production debugging using trace IDs
 *
 * Architecture:
 * - Application layer (coordinates async logging)
 * - Depends on domain port (AuditLogRepositoryPort)
 * - Uses @Async("taskExecutor") with MdcTaskDecorator for trace ID propagation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepositoryPort auditLogRepository;

    /**
     * Logs a payment attempt event.
     * Called BEFORE attempting payment with gateway.
     *
     * @param orderId the order ID
     * @param attemptNumber current attempt number (1, 2, 3...)
     * @param maxRetries maximum retries allowed
     */
    @Async("taskExecutor")
    public void logPaymentAttempt(UUID orderId, int attemptNumber, int maxRetries) {
        try {
            String traceId = MDC.get("traceId");

            log.debug("[AUDIT] Logging payment attempt {}/{} for order: {}, traceId: {}",
                    attemptNumber, maxRetries, orderId, traceId);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .traceId(traceId)
                    .eventType(EventType.PAYMENT_ATTEMPT.name())
                    .entityType("Order")
                    .entityId(orderId)
                    .status("IN_PROGRESS")
                    .eventData(String.format("{\"attemptNumber\":%d,\"maxRetries\":%d}",
                            attemptNumber, maxRetries))
                    .build();

            auditLogRepository.save(auditLog);

            log.info("[AUDIT] Payment attempt logged: order={}, attempt={}/{}, traceId={}",
                    orderId, attemptNumber, maxRetries, traceId);

        } catch (Exception e) {
            // Never let audit logging failures affect business flow
            log.error("[AUDIT] Failed to log payment attempt for order: {}", orderId, e);
        }
    }

    /**
     * Logs a successful payment event.
     * Called AFTER payment is confirmed by gateway.
     *
     * @param orderId the order ID
     * @param transactionId payment gateway transaction ID
     * @param attemptNumber attempt number that succeeded
     */
    @Async("taskExecutor")
    public void logPaymentSuccess(UUID orderId, String transactionId, int attemptNumber) {
        try {
            String traceId = MDC.get("traceId");

            log.debug("[AUDIT] Logging payment success for order: {}, txId: {}, traceId: {}",
                    orderId, transactionId, traceId);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .traceId(traceId)
                    .eventType(EventType.PAYMENT_SUCCESS.name())
                    .entityType("Order")
                    .entityId(orderId)
                    .status("SUCCESS")
                    .eventData(String.format("{\"transactionId\":\"%s\",\"attemptNumber\":%d}",
                            transactionId, attemptNumber))
                    .build();

            auditLogRepository.save(auditLog);

            log.info("[AUDIT] Payment success logged: order={}, txId={}, attempt={}, traceId={}",
                    orderId, transactionId, attemptNumber, traceId);

        } catch (Exception e) {
            log.error("[AUDIT] Failed to log payment success for order: {}", orderId, e);
        }
    }

    /**
     * Logs a failed payment event.
     * Called AFTER all payment retries are exhausted.
     *
     * @param orderId the order ID
     * @param errorMessage reason for payment failure
     * @param totalAttempts total attempts made
     */
    @Async("taskExecutor")
    public void logPaymentFailure(UUID orderId, String errorMessage, int totalAttempts) {
        try {
            String traceId = MDC.get("traceId");

            log.debug("[AUDIT] Logging payment failure for order: {}, error: {}, traceId: {}",
                    orderId, errorMessage, traceId);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .traceId(traceId)
                    .eventType(EventType.PAYMENT_FAILED.name())
                    .entityType("Order")
                    .entityId(orderId)
                    .status("FAILURE")
                    .errorMessage(errorMessage)
                    .eventData(String.format("{\"totalAttempts\":%d}", totalAttempts))
                    .build();

            auditLogRepository.save(auditLog);

            log.info("[AUDIT] Payment failure logged: order={}, attempts={}, traceId={}",
                    orderId, totalAttempts, traceId);

        } catch (Exception e) {
            log.error("[AUDIT] Failed to log payment failure for order: {}", orderId, e);
        }
    }

    /**
     * Generic method to log any event with EventType enum.
     * Preferred method for type safety.
     *
     * @param eventType type of event (enum)
     * @param entityType type of entity (e.g., "Order", "Product")
     * @param entityId ID of the entity
     * @param status status of the event (e.g., "SUCCESS", "FAILURE")
     * @param eventData JSON metadata (null if not needed)
     */
    @Async("taskExecutor")
    public void logEvent(
            EventType eventType,
            String entityType,
            UUID entityId,
            String status,
            String eventData
    ) {
        try {
            String traceId = MDC.get("traceId");

            log.debug("[AUDIT] Logging event: type={}, entity={}:{}, status={}, traceId={}",
                    eventType, entityType, entityId, status, traceId);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .traceId(traceId)
                    .eventType(eventType.name())
                    .entityType(entityType)
                    .entityId(entityId)
                    .status(status)
                    .eventData(eventData)
                    .build();

            auditLogRepository.save(auditLog);

            log.info("[AUDIT] Event logged: type={}, entity={}:{}, traceId={}",
                    eventType, entityType, entityId, traceId);

        } catch (Exception e) {
            log.error("[AUDIT] Failed to log event: type={}, entity={}:{}",
                    eventType, entityType, entityId, e);
        }
    }

    /**
     * Generic method to log any event with error message.
     * Convenience method for error scenarios.
     *
     * @param eventType type of event (enum)
     * @param entityType type of entity (e.g., "Order", "Product", "System")
     * @param entityId ID of the entity (null for system errors)
     * @param status status of the event (e.g., "SUCCESS", "FAILURE")
     * @param errorMessage error message (null if successful)
     */
    @Async("taskExecutor")
    public void logEventWithError(
            EventType eventType,
            String entityType,
            UUID entityId,
            String status,
            String errorMessage
    ) {
        try {
            String traceId = MDC.get("traceId");

            log.debug("[AUDIT] Logging event with error: type={}, entity={}:{}, status={}, traceId={}",
                    eventType, entityType, entityId, status, traceId);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .traceId(traceId)
                    .eventType(eventType.name())
                    .entityType(entityType)
                    .entityId(entityId)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(auditLog);

            log.info("[AUDIT] Event with error logged: type={}, entity={}:{}, traceId={}",
                    eventType, entityType, entityId, traceId);

        } catch (Exception e) {
            log.error("[AUDIT] Failed to log event: type={}, entity={}:{}",
                    eventType, entityType, entityId, e);
        }
    }

    /**
     * Generic method to log any event (String-based, for backwards compatibility).
     * DEPRECATED: Use logEvent(EventType, ...) instead for type safety.
     *
     * @param eventType type of event (e.g., "ORDER_CREATED", "STOCK_REDUCED")
     * @param entityType type of entity (e.g., "Order", "Product")
     * @param entityId ID of the entity
     * @param status status of the event (e.g., "SUCCESS", "FAILURE")
     * @param errorMessage error message (null if successful)
     */
    @Deprecated
    @Async("taskExecutor")
    public void logEvent(
            String eventType,
            String entityType,
            UUID entityId,
            String status,
            String errorMessage
    ) {
        try {
            String traceId = MDC.get("traceId");

            log.debug("[AUDIT] Logging event: type={}, entity={}:{}, status={}, traceId={}",
                    eventType, entityType, entityId, status, traceId);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .traceId(traceId)
                    .eventType(eventType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(auditLog);

            log.info("[AUDIT] Event logged: type={}, entity={}:{}, traceId={}",
                    eventType, entityType, entityId, traceId);

        } catch (Exception e) {
            log.error("[AUDIT] Failed to log event: type={}, entity={}:{}",
                    eventType, entityType, entityId, e);
        }
    }
}
