package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import com.farmatodo.reto_tecnico.domain.exception.PaymentFailedException;
import com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException;
import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import com.farmatodo.reto_tecnico.domain.port.in.ProcessPaymentUseCase.PaymentResult;
import com.farmatodo.reto_tecnico.domain.port.in.TokenizeCardUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.PaymentGatewayPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
/**
 * Unit tests for PaymentService.
 * Tests payment processing with retry logic and failure scenarios.
 *
 * Uses pure unit testing with Mockito (NO Spring context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private TokenizeCardUseCase tokenizationService;

    @Mock
    private PaymentGatewayPort paymentGateway;

    @Mock
    private PaymentTransactionService transactionService;

    @Mock
    private FarmatodoProperties properties;

    @Mock
    private FarmatodoProperties.Payment paymentConfig;

    @Mock
    private AsyncEmailService asyncEmailService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;
    private CreditCard testCard;
    private CreditCard tokenizedCard;

    @BeforeEach
    void setUp() {
        // Configure properties mock (lenient to avoid UnnecessaryStubbingException)
        lenient().when(properties.getPayment()).thenReturn(paymentConfig);
        lenient().when(paymentConfig.getMaxRetries()).thenReturn(3);
        lenient().when(paymentConfig.getRetryDelayMillis()).thenReturn(100L); // Short delay for tests
        lenient().when(paymentConfig.getRejectionProbability()).thenReturn(0); // No random failures in tests

        // Create test order
        testOrder = mock(Order.class);
        when(testOrder.getId()).thenReturn(UUID.randomUUID());

        // Create test credit card
        testCard = CreditCard.builder()
                .cardNumber(new CardNumber("4532015112830366"))
                .cardholderName("John Doe")
                .expirationDate("12/25")
                .cvv("123")
                .build();

        // Create tokenized card
        tokenizedCard = CreditCard.builder()
                .cardNumber(new CardNumber("4532015112830366"))
                .cardholderName("John Doe")
                .expirationDate("12/25")
                .cvv("123")
                .build();
        tokenizedCard.assignToken("tok_test123");
    }

    @Test
    @DisplayName("Should process payment successfully on first attempt")
    void shouldProcessPaymentSuccessfully() {
        // Given: Tokenization succeeds and payment gateway approves immediately
        when(tokenizationService.tokenize(testCard)).thenReturn(tokenizedCard);
        when(transactionService.assignTokenAndSave(any(Order.class), anyString())).thenReturn(testOrder);
        when(transactionService.confirmPaymentAndSave(any(Order.class))).thenReturn(testOrder);

        // Mock payment gateway to return success immediately
        // Need to ensure attemptPayment succeeds by setting rejection probability to 0
        when(paymentConfig.getRejectionProbability()).thenReturn(0);

        // Act
        PaymentResult result = paymentService.processPayment(testOrder, testCard);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isNotNull();
        assertThat(result.transactionId()).startsWith("txn_");
        assertThat(result.attemptsMade()).isEqualTo(1);
        assertThat(result.message()).isEqualTo("Payment successful");

        // Verify interactions
        verify(tokenizationService, times(1)).tokenize(testCard);
        verify(transactionService, times(1)).assignTokenAndSave(testOrder, "tok_test123");
        verify(transactionService, times(1)).confirmPaymentAndSave(testOrder);
        verify(transactionService, never()).failPaymentAndSave(any());
    }

    @Test
    @DisplayName("Should retry payment and succeed on 3rd attempt")
    void shouldRetryPaymentAndSucceedOnThirdAttempt() {
        // Given: Tokenization succeeds, payment fails 2 times then succeeds
        when(tokenizationService.tokenize(testCard)).thenReturn(tokenizedCard);
        when(transactionService.assignTokenAndSave(any(Order.class), anyString())).thenReturn(testOrder);
        when(transactionService.confirmPaymentAndSave(any(Order.class))).thenReturn(testOrder);

        // Mock rejection probability to simulate failures
        // Attempt 1: 99 (fail)
        // Attempt 2: 99 (fail)
        // Attempt 3: 0 (success)
        when(paymentConfig.getRejectionProbability())
                .thenReturn(100)  // First call - will fail
                .thenReturn(100)  // Second call - will fail
                .thenReturn(0);   // Third call - will succeed

        // Act
        PaymentResult result = paymentService.processPayment(testOrder, testCard);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.attemptsMade()).isEqualTo(3);
        assertThat(result.transactionId()).isNotNull();

        // Verify confirmPayment was called once (on success)
        verify(transactionService, times(1)).confirmPaymentAndSave(testOrder);
        verify(transactionService, never()).failPaymentAndSave(any());
    }

    @Test
    @DisplayName("Should fail payment after all retries exhausted")
    void shouldFailPaymentAfterAllRetriesExhausted() {
        // Given: Tokenization succeeds, but all payment attempts fail
        when(tokenizationService.tokenize(testCard)).thenReturn(tokenizedCard);
        when(transactionService.assignTokenAndSave(any(Order.class), anyString())).thenReturn(testOrder);
        when(transactionService.failPaymentAndSave(any(Order.class))).thenReturn(testOrder);

        // Mock rejection probability to always fail
        when(paymentConfig.getRejectionProbability()).thenReturn(100);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(testOrder, testCard))
                .isInstanceOf(PaymentFailedException.class)
                .hasMessageContaining("Payment rejected after 3 attempts");

        // Verify failPayment was called (at least once, may be called in retry loop and catch block)
        verify(transactionService, atLeast(1)).failPaymentAndSave(testOrder);
        verify(transactionService, never()).confirmPaymentAndSave(any());
    }

    @Test
    @DisplayName("Should handle tokenization failure")
    void shouldHandleTokenizationFailure() {
        // Given: Tokenization fails
        when(tokenizationService.tokenize(testCard))
                .thenThrow(new TokenizationFailedException("Invalid card number"));
        when(transactionService.failPaymentAndSave(any(Order.class))).thenReturn(testOrder);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(testOrder, testCard))
                .isInstanceOf(PaymentFailedException.class)
                .hasMessageContaining("tokenization failed");

        // Verify failPayment was called
        verify(transactionService, times(1)).failPaymentAndSave(testOrder);
        verify(transactionService, never()).confirmPaymentAndSave(any());
        verify(transactionService, never()).assignTokenAndSave(any(), any());
    }

    @Test
    @DisplayName("Should handle unexpected exception during payment")
    void shouldHandleUnexpectedException() {
        // Given: Tokenization succeeds but unexpected error occurs
        when(tokenizationService.tokenize(testCard)).thenReturn(tokenizedCard);
        when(transactionService.assignTokenAndSave(any(Order.class), anyString()))
                .thenThrow(new RuntimeException("Database error"));
        when(transactionService.failPaymentAndSave(any(Order.class))).thenReturn(testOrder);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(testOrder, testCard))
                .isInstanceOf(PaymentFailedException.class)
                .hasMessageContaining("Unexpected error processing payment");

        // Verify failPayment was called
        verify(transactionService, times(1)).failPaymentAndSave(testOrder);
    }
}
