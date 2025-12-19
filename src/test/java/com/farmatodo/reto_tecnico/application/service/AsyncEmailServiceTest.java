package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.port.out.EmailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AsyncEmailService.
 *
 * Note: Since the methods are @Async, in unit tests they will execute synchronously
 * because there's no Spring context. This is acceptable for unit tests.
 * Integration tests would test the actual async behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AsyncEmailService Unit Tests")
class AsyncEmailServiceTest {

    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private AsyncEmailService asyncEmailService;

    private Order testOrder;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(new Email("juan.perez@example.com"))
                .build();

        Product testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .description("Test Description")
                .price(new Money(new BigDecimal("10.00")))
                .stock(10)
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(new Money(new BigDecimal("10.00")))
                .build();

        testOrder = Order.create(testCustomer, List.of(item));
    }

    @Test
    @DisplayName("Should send payment success email asynchronously")
    void shouldSendPaymentSuccessEmailAsync() {
        // Given
        String transactionId = "txn_123456";

        // When
        asyncEmailService.sendPaymentSuccessEmailAsync(testOrder, transactionId);

        // Then
        verify(emailPort, times(1)).sendPaymentSuccessEmail(
                eq("juan.perez@example.com"),
                eq("Juan Pérez"),
                eq(testOrder.getId().toString()),
                eq(testOrder.getTotalAmount().toString()),
                eq(transactionId)
        );
    }

    @Test
    @DisplayName("Should send payment failure email asynchronously")
    void shouldSendPaymentFailureEmailAsync() {
        // Given
        int attempts = 3;

        // When
        asyncEmailService.sendPaymentFailureEmailAsync(testOrder, attempts);

        // Then
        verify(emailPort, times(1)).sendPaymentFailureEmail(
                eq("juan.perez@example.com"),
                eq("Juan Pérez"),
                eq(testOrder.getId().toString()),
                eq(testOrder.getTotalAmount().toString()),
                eq(attempts)
        );
    }

    @Test
    @DisplayName("Should send generic email asynchronously")
    void shouldSendEmailAsync() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        asyncEmailService.sendEmailAsync(to, subject, body);

        // Then
        verify(emailPort, times(1)).sendEmail(to, subject, body);
    }

    @Test
    @DisplayName("Should handle exception in payment success email gracefully")
    void shouldHandleExceptionInPaymentSuccessEmailGracefully() {
        // Given
        String transactionId = "txn_123456";
        doThrow(new RuntimeException("SMTP server error"))
                .when(emailPort).sendPaymentSuccessEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        // When - should not throw exception
        asyncEmailService.sendPaymentSuccessEmailAsync(testOrder, transactionId);

        // Then
        verify(emailPort, times(1)).sendPaymentSuccessEmail(
                anyString(), anyString(), anyString(), anyString(), anyString()
        );
        // No exception should be thrown - method should handle it gracefully
    }

    @Test
    @DisplayName("Should handle exception in payment failure email gracefully")
    void shouldHandleExceptionInPaymentFailureEmailGracefully() {
        // Given
        int attempts = 3;
        doThrow(new RuntimeException("SMTP server error"))
                .when(emailPort).sendPaymentFailureEmail(anyString(), anyString(), anyString(), anyString(), anyInt());

        // When - should not throw exception
        asyncEmailService.sendPaymentFailureEmailAsync(testOrder, attempts);

        // Then
        verify(emailPort, times(1)).sendPaymentFailureEmail(
                anyString(), anyString(), anyString(), anyString(), anyInt()
        );
        // No exception should be thrown - method should handle it gracefully
    }

    @Test
    @DisplayName("Should handle exception in generic email gracefully")
    void shouldHandleExceptionInGenericEmailGracefully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        doThrow(new RuntimeException("SMTP server error"))
                .when(emailPort).sendEmail(anyString(), anyString(), anyString());

        // When - should not throw exception
        asyncEmailService.sendEmailAsync(to, subject, body);

        // Then
        verify(emailPort, times(1)).sendEmail(to, subject, body);
        // No exception should be thrown - method should handle it gracefully
    }

    @Test
    @DisplayName("Should extract correct customer email from order")
    void shouldExtractCorrectCustomerEmailFromOrder() {
        // Given
        String transactionId = "txn_789";

        // When
        asyncEmailService.sendPaymentSuccessEmailAsync(testOrder, transactionId);

        // Then
        verify(emailPort).sendPaymentSuccessEmail(
                eq("juan.perez@example.com"), // Verify email is correctly extracted
                eq("Juan Pérez"),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should extract correct order total from order")
    void shouldExtractCorrectOrderTotalFromOrder() {
        // Given
        String transactionId = "txn_456";

        // When
        asyncEmailService.sendPaymentSuccessEmailAsync(testOrder, transactionId);

        // Then
        verify(emailPort).sendPaymentSuccessEmail(
                anyString(),
                anyString(),
                anyString(),
                eq(testOrder.getTotalAmount().toString()), // Verify total is correctly extracted
                anyString()
        );
    }
}
