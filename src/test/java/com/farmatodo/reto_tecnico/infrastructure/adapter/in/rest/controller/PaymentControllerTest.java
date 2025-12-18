package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.PaymentFailedException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.in.ProcessPaymentUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CreditCardRestMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for PaymentController.
 * Tests payment processing and HTTP status code 402 (Payment Required).
 *
 * Uses CreditCardRestMapperImpl for DTO conversion.
 */
@WebMvcTest(PaymentController.class)
@Import(CreditCardRestMapperImpl.class)
@DisplayName("PaymentController REST Tests")
class PaymentControllerTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "default-dev-key-change-in-production";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessPaymentUseCase processPaymentUseCase;

    @MockBean
    private OrderRepositoryPort orderRepository;

    private Order testOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        Customer testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(new Email("juan@test.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123, Bogotá")
                .build();

        Product testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        OrderItem orderItem = OrderItem.create(testProduct, 2);
        testOrder = Order.create(testCustomer, List.of(orderItem));
        orderId = testOrder.getId();
    }

    @Test
    @DisplayName("Should process payment successfully with 200 status")
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given: Valid payment request
        String requestBody = """
            {
                "creditCard": {
                    "customerId": "123e4567-e89b-12d3-a456-426614174000",
                    "cardNumber": "4532015112830366",
                    "cvv": "123",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // Mock order retrieval
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // Mock successful payment processing
        ProcessPaymentUseCase.PaymentResult successResult =
                new ProcessPaymentUseCase.PaymentResult(true, "txn_123456", "Payment successful", 1);
        when(processPaymentUseCase.processPayment(any(Order.class), any())).thenReturn(successResult);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("txn_123456"))
                .andExpect(jsonPath("$.attempts").value(1))
                .andExpect(jsonPath("$.message").value(containsString("successfully")))
                .andExpect(jsonPath("$.orderStatus").exists());

        // Verify service calls
        verify(orderRepository, times(1)).findById(orderId);
        verify(processPaymentUseCase, times(1)).processPayment(any(Order.class), any());
    }

    @Test
    @DisplayName("Should return 402 Payment Required when payment fails")
    void shouldReturnPaymentRequiredWhenFailed() throws Exception {
        // Given: Valid payment request but payment will fail
        String requestBody = """
            {
                "creditCard": {
                    "customerId": "123e4567-e89b-12d3-a456-426614174000",
                    "cardNumber": "4532015112830366",
                    "cvv": "123",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // Mock order retrieval
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // Mock payment failure after retries
        when(processPaymentUseCase.processPayment(any(Order.class), any()))
                .thenThrow(new PaymentFailedException(orderId, "Payment gateway rejected transaction", 3));

        // When & Then: Verify 402 status
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value(402))
                .andExpect(jsonPath("$.error").value("PAYMENT_FAILED"))
                .andExpect(jsonPath("$.message").value(containsString("Payment failed")));

        verify(orderRepository, times(1)).findById(orderId);
        verify(processPaymentUseCase, times(1)).processPayment(any(Order.class), any());
    }

    @Test
    @DisplayName("Should return 404 Not Found when order doesn't exist")
    void shouldReturnNotFoundWhenOrderDoesntExist() throws Exception {
        // Given: Non-existent order
        UUID nonExistentOrderId = UUID.randomUUID();
        String requestBody = """
            {
                "creditCard": {
                    "customerId": "123e4567-e89b-12d3-a456-426614174000",
                    "cardNumber": "4532015112830366",
                    "cvv": "123",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // Mock order not found
        when(orderRepository.findById(nonExistentOrderId))
                .thenThrow(new OrderNotFoundException(nonExistentOrderId));

        // When & Then: Verify 404 status
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", nonExistentOrderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        verify(orderRepository, times(1)).findById(nonExistentOrderId);
        verify(processPaymentUseCase, never()).processPayment(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when card number is invalid")
    void shouldReturnBadRequestWhenCardNumberInvalid() throws Exception {
        // Given: Invalid card number (too short)
        String requestBody = """
            {
                "creditCard": {
                    "cardNumber": "123",
                    "cvv": "123",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors['creditCard.cardNumber']").exists());

        verify(orderRepository, never()).findById(any());
        verify(processPaymentUseCase, never()).processPayment(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when CVV is invalid")
    void shouldReturnBadRequestWhenCvvInvalid() throws Exception {
        // Given: Invalid CVV (letters)
        String requestBody = """
            {
                "creditCard": {
                    "cardNumber": "4532015112830366",
                    "cvv": "ABC",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors['creditCard.cvv']").value(containsString("digits")));

        verify(processPaymentUseCase, never()).processPayment(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when expiration date format is invalid")
    void shouldReturnBadRequestWhenExpirationDateFormatInvalid() throws Exception {
        // Given: Invalid expiration date format
        String requestBody = """
            {
                "creditCard": {
                    "cardNumber": "4532015112830366",
                    "cvv": "123",
                    "expirationDate": "2025-12",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors['creditCard.expirationDate']").value(containsString("MM/YY")));

        verify(processPaymentUseCase, never()).processPayment(any(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when cardholder name contains numbers")
    void shouldReturnBadRequestWhenCardholderNameContainsNumbers() throws Exception {
        // Given: Invalid cardholder name (contains numbers)
        String requestBody = """
            {
                "creditCard": {
                    "cardNumber": "4532015112830366",
                    "cvv": "123",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN123"
                }
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors['creditCard.cardholderName']").exists());

        verify(processPaymentUseCase, never()).processPayment(any(), any());
    }

    @Test
    @DisplayName("Should handle payment with multiple retry attempts")
    void shouldHandlePaymentWithMultipleRetries() throws Exception {
        // Given: Payment succeeds on 3rd attempt
        String requestBody = """
            {
                "creditCard": {
                    "customerId": "123e4567-e89b-12d3-a456-426614174000",
                    "cardNumber": "4532015112830366",
                    "cvv": "123",
                    "expirationDate": "12/25",
                    "cardholderName": "JUAN PEREZ"
                }
            }
            """;

        // Mock order retrieval
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // Mock payment success after 3 attempts
        ProcessPaymentUseCase.PaymentResult result =
                new ProcessPaymentUseCase.PaymentResult(true, "txn_789", "Payment successful on attempt 3", 3);
        when(processPaymentUseCase.processPayment(any(Order.class), any())).thenReturn(result);

        // When & Then: Verify response includes attempt count
        mockMvc.perform(post("/api/v1/payments/orders/{orderId}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.attempts").value(3))
                .andExpect(jsonPath("$.message").value(containsString("attempt 3")));

        verify(processPaymentUseCase, times(1)).processPayment(any(Order.class), any());
    }
}
