package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException;
import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import com.farmatodo.reto_tecnico.domain.port.in.TokenizeCardUseCase;
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

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for CardController.
 * Tests credit card tokenization endpoint with validation and error scenarios.
 *
 * SECURITY: Tests ensure sensitive card data is properly handled.
 */
@WebMvcTest(CardController.class)
@Import(CreditCardRestMapperImpl.class)
@DisplayName("CardController REST Tests")
class CardControllerTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "default-dev-key-change-in-production";
    private static final String TOKENIZE_URL = "/api/v1/cards/tokenize";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenizeCardUseCase tokenizeCardUseCase;

    @MockBean
    private com.farmatodo.reto_tecnico.application.service.AuditLogService auditLogService;

    private CreditCard tokenizedCard;
    private static final UUID TEST_CUSTOMER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        // Set up a valid tokenized card for success scenarios
        tokenizedCard = CreditCard.builder()
                .id(UUID.randomUUID())
                .customerId(TEST_CUSTOMER_ID)
                .cardNumber(new CardNumber("4532015112830366"))
                .cvv("123")
                .expirationDate("12/25")
                .cardholderName("JUAN PEREZ")
                .token("tok_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .build();
    }

    @Test
    @DisplayName("Should tokenize card successfully with 200 status")
    void shouldTokenizeCardSuccessfully() throws Exception {
        // Given: Valid tokenization request
        String requestBody = """
            {
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "cardNumber": "4532015112830366",
                "cvv": "123",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // Mock successful tokenization
        when(tokenizeCardUseCase.tokenize(any(CreditCard.class))).thenReturn(tokenizedCard);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.maskedCardNumber").value("************0366"))
                .andExpect(jsonPath("$.lastFourDigits").value("0366"))
                .andExpect(jsonPath("$.expirationDate").value("12/25"))
                .andExpect(jsonPath("$.cardholderName").value("JUAN PEREZ"));

        // Verify use case was called
        verify(tokenizeCardUseCase, times(1)).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when card number is too short")
    void shouldReturn400WhenCardNumberTooShort() throws Exception {
        // Given: Invalid card number (too short)
        String requestBody = """
            {
                "cardNumber": "123",
                "cvv": "123",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.cardNumber").value(containsString("13 and 19 digits")));

        // Use case should NOT be called due to validation failure
        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when card number contains letters")
    void shouldReturn400WhenCardNumberContainsLetters() throws Exception {
        // Given: Invalid card number (contains letters)
        String requestBody = """
            {
                "cardNumber": "4532ABCD11283036",
                "cvv": "123",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.cardNumber").exists());

        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when CVV is invalid")
    void shouldReturn400WhenCvvInvalid() throws Exception {
        // Given: Invalid CVV (letters instead of digits)
        String requestBody = """
            {
                "cardNumber": "4532015112830366",
                "cvv": "ABC",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.cvv").value(containsString("3 or 4 digits")));

        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when expiration date format is invalid")
    void shouldReturn400WhenExpirationDateFormatInvalid() throws Exception {
        // Given: Invalid expiration date format (YYYY-MM instead of MM/YY)
        String requestBody = """
            {
                "cardNumber": "4532015112830366",
                "cvv": "123",
                "expirationDate": "2025-12",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.expirationDate").value(containsString("MM/YY")));

        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when cardholder name contains numbers")
    void shouldReturn400WhenCardholderNameContainsNumbers() throws Exception {
        // Given: Invalid cardholder name (contains numbers)
        String requestBody = """
            {
                "cardNumber": "4532015112830366",
                "cvv": "123",
                "expirationDate": "12/25",
                "cardholderName": "JUAN123 PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.cardholderName").value(containsString("letters and spaces")));

        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when tokenization fails")
    void shouldReturn400WhenTokenizationFails() throws Exception {
        // Given: Valid request but tokenization fails
        String requestBody = """
            {
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "cardNumber": "4532015112830366",
                "cvv": "123",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // Mock tokenization failure
        when(tokenizeCardUseCase.tokenize(any(CreditCard.class)))
                .thenThrow(new TokenizationFailedException("Tokenization rejected by gateway"));

        // When & Then: Verify error response
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("TOKENIZATION_FAILED"))
                .andExpect(jsonPath("$.message").value(containsString("tokenization failed")));

        verify(tokenizeCardUseCase, times(1)).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when card number is blank")
    void shouldReturn400WhenCardNumberBlank() throws Exception {
        // Given: Blank card number
        String requestBody = """
            {
                "cardNumber": "",
                "cvv": "123",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.cardNumber").exists());

        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when CVV is blank")
    void shouldReturn400WhenCvvBlank() throws Exception {
        // Given: Blank CVV
        String requestBody = """
            {
                "cardNumber": "4532015112830366",
                "cvv": "",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.cvv").exists());

        verify(tokenizeCardUseCase, never()).tokenize(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should accept 4-digit CVV (AMEX cards)")
    void shouldAccept4DigitCvv() throws Exception {
        // Given: Valid request with 4-digit CVV (AMEX format)
        String requestBody = """
            {
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "cardNumber": "4532015112830366",
                "cvv": "1234",
                "expirationDate": "12/25",
                "cardholderName": "JUAN PEREZ"
            }
            """;

        // Mock successful tokenization
        when(tokenizeCardUseCase.tokenize(any(CreditCard.class))).thenReturn(tokenizedCard);

        // When & Then: Should succeed with 4-digit CVV
        mockMvc.perform(post(TOKENIZE_URL)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        verify(tokenizeCardUseCase, times(1)).tokenize(any(CreditCard.class));
    }
}
