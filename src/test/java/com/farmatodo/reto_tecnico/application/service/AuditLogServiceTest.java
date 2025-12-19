package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.AuditLog;
import com.farmatodo.reto_tecnico.domain.port.out.AuditLogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogService.
 *
 * Note: Since methods are @Async, in unit tests they execute synchronously
 * because there's no Spring context. This is acceptable for unit tests.
 * Integration tests would test actual async behavior with MDC propagation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Unit Tests")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private UUID testOrderId;
    private String testTraceId;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testTraceId = UUID.randomUUID().toString();

        // Set up MDC for testing
        MDC.put("traceId", testTraceId);

        // Mock repository to return the input (lenient to avoid UnnecessaryStubbingException)
        lenient().when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should log payment attempt with trace ID from MDC")
    void shouldLogPaymentAttempt() {
        // Given
        int attemptNumber = 1;
        int maxRetries = 3;

        // When
        auditLogService.logPaymentAttempt(testOrderId, attemptNumber, maxRetries);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo(testTraceId);
        assertThat(saved.getEventType()).isEqualTo("PAYMENT_ATTEMPT");
        assertThat(saved.getEntityType()).isEqualTo("Order");
        assertThat(saved.getEntityId()).isEqualTo(testOrderId);
        assertThat(saved.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(saved.getEventData()).contains("\"attemptNumber\":1");
        assertThat(saved.getEventData()).contains("\"maxRetries\":3");
    }

    @Test
    @DisplayName("Should log payment success with trace ID and transaction ID")
    void shouldLogPaymentSuccess() {
        // Given
        String transactionId = "txn_123456";
        int attemptNumber = 2;

        // When
        auditLogService.logPaymentSuccess(testOrderId, transactionId, attemptNumber);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo(testTraceId);
        assertThat(saved.getEventType()).isEqualTo("PAYMENT_SUCCESS");
        assertThat(saved.getEntityType()).isEqualTo("Order");
        assertThat(saved.getEntityId()).isEqualTo(testOrderId);
        assertThat(saved.getStatus()).isEqualTo("SUCCESS");
        assertThat(saved.getEventData()).contains("\"transactionId\":\"txn_123456\"");
        assertThat(saved.getEventData()).contains("\"attemptNumber\":2");
    }

    @Test
    @DisplayName("Should log payment failure with trace ID and error message")
    void shouldLogPaymentFailure() {
        // Given
        String errorMessage = "Payment rejected after 3 attempts";
        int totalAttempts = 3;

        // When
        auditLogService.logPaymentFailure(testOrderId, errorMessage, totalAttempts);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo(testTraceId);
        assertThat(saved.getEventType()).isEqualTo("PAYMENT_FAILED");
        assertThat(saved.getEntityType()).isEqualTo("Order");
        assertThat(saved.getEntityId()).isEqualTo(testOrderId);
        assertThat(saved.getStatus()).isEqualTo("FAILURE");
        assertThat(saved.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(saved.getEventData()).contains("\"totalAttempts\":3");
    }

    @Test
    @DisplayName("Should log generic event with all parameters")
    void shouldLogGenericEvent() {
        // Given
        String eventType = "ORDER_CREATED";
        String entityType = "Order";
        String status = "SUCCESS";
        String errorMessage = null;

        // When
        auditLogService.logEvent(eventType, entityType, testOrderId, status, errorMessage);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo(testTraceId);
        assertThat(saved.getEventType()).isEqualTo(eventType);
        assertThat(saved.getEntityType()).isEqualTo(entityType);
        assertThat(saved.getEntityId()).isEqualTo(testOrderId);
        assertThat(saved.getStatus()).isEqualTo(status);
        assertThat(saved.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle null trace ID gracefully")
    void shouldHandleNullTraceId() {
        // Given
        MDC.remove("traceId"); // Simulate missing trace ID

        // When
        auditLogService.logPaymentAttempt(testOrderId, 1, 3);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isNull(); // Should save null if not present
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When - should not throw exception
        auditLogService.logPaymentAttempt(testOrderId, 1, 3);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        // No exception should be thrown - method handles it gracefully
    }
}
