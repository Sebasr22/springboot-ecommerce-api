package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException;
import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenizationServiceImpl.
 * Tests tokenization with probability-based success/failure scenarios.
 *
 * Uses pure unit testing with Mockito (NO Spring context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenizationServiceImpl Unit Tests")
class TokenizationServiceImplTest {

    @Mock
    private FarmatodoProperties properties;

    @Mock
    private FarmatodoProperties.Tokenization tokenizationConfig;

    @InjectMocks
    private TokenizationServiceImpl tokenizationService;

    private CreditCard validCard;

    @BeforeEach
    void setUp() {
        // Configure properties mock
        lenient().when(properties.getTokenization()).thenReturn(tokenizationConfig);

        // Create valid credit card (Visa, not expired, valid Luhn)
        validCard = CreditCard.builder()
                .cardNumber(new CardNumber("4532015112830366")) // Valid Visa with Luhn
                .cardholderName("John Doe")
                .expirationDate("12/25")
                .cvv("123")
                .build();
    }

    @Test
    @DisplayName("Should tokenize successfully when probability is 0% (never fails)")
    void shouldTokenizeSuccessfullyWhenProbabilityIsZero() {
        // Given: 0% rejection probability (always succeeds)
        when(tokenizationConfig.getRejectionProbability()).thenReturn(0);

        // Act
        CreditCard tokenizedCard = tokenizationService.tokenize(validCard);

        // Assert
        assertThat(tokenizedCard.isTokenized()).isTrue();
        assertThat(tokenizedCard.getToken()).isNotNull();
        assertThat(tokenizedCard.getToken()).startsWith("tok_");
        assertThat(tokenizedCard.getCvv()).isNull(); // Sensitive data cleared
    }

    @Test
    @DisplayName("Should throw exception when probability is 100% (always fails)")
    void shouldThrowExceptionWhenProbabilityIs100() {
        // Given: 100% rejection probability (always fails)
        when(tokenizationConfig.getRejectionProbability()).thenReturn(100);

        // Act & Assert
        assertThatThrownBy(() -> tokenizationService.tokenize(validCard))
                .isInstanceOf(TokenizationFailedException.class)
                .hasMessageContaining("Tokenization service rejected the card");

        // Verify token was never assigned
        assertThat(validCard.isTokenized()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for expired card")
    void shouldThrowExceptionForExpiredCard() {
        // Given: Expired card
        CreditCard expiredCard = CreditCard.builder()
                .cardNumber(new CardNumber("4532015112830366"))
                .cardholderName("John Doe")
                .expirationDate("12/20") // Expired (year 2020)
                .cvv("123")
                .build();

        // No need to set rejection probability - validation happens before

        // Act & Assert
        assertThatThrownBy(() -> tokenizationService.tokenize(expiredCard))
                .isInstanceOf(TokenizationFailedException.class)
                .hasMessageContaining("Credit card has expired");
    }

    @Test
    @DisplayName("Should throw exception for invalid Luhn check")
    void shouldThrowExceptionForInvalidLuhnCheck() {
        // Given: Card with invalid Luhn check
        CreditCard invalidCard = CreditCard.builder()
                .cardNumber(new CardNumber("4532015112830367")) // Invalid Luhn (last digit wrong)
                .cardholderName("John Doe")
                .expirationDate("12/25")
                .cvv("123")
                .build();

        // No need to set rejection probability - validation happens before

        // Act & Assert
        assertThatThrownBy(() -> tokenizationService.tokenize(invalidCard))
                .isInstanceOf(TokenizationFailedException.class)
                .hasMessageContaining("Invalid credit card number")
                .hasMessageContaining("Luhn check failed");
    }

    @Test
    @DisplayName("Should validate token correctly")
    void shouldValidateTokenCorrectly() {
        // Valid tokens
        assertThat(tokenizationService.validateToken("tok_12345678901234567890")).isTrue();
        assertThat(tokenizationService.validateToken("tok_abcdefghijklmnopqrstuvwxyz")).isTrue();

        // Invalid tokens
        assertThat(tokenizationService.validateToken(null)).isFalse();
        assertThat(tokenizationService.validateToken("")).isFalse();
        assertThat(tokenizationService.validateToken("   ")).isFalse();
        assertThat(tokenizationService.validateToken("tok_short")).isFalse(); // Too short
        assertThat(tokenizationService.validateToken("invalid_token_format")).isFalse(); // Wrong prefix
    }

    @Test
    @DisplayName("Should generate token with correct format")
    void shouldGenerateTokenWithCorrectFormat() {
        // Given: 0% rejection probability
        when(tokenizationConfig.getRejectionProbability()).thenReturn(0);

        // Act
        CreditCard tokenizedCard = tokenizationService.tokenize(validCard);

        // Assert: Token format
        String token = tokenizedCard.getToken();
        assertThat(token).startsWith("tok_");
        assertThat(token).hasSize(36); // "tok_" (4 chars) + UUID without dashes (32 chars)
        assertThat(token).doesNotContain("-"); // UUID dashes removed
    }

    @Test
    @DisplayName("Should clear sensitive data after tokenization")
    void shouldClearSensitiveDataAfterTokenization() {
        // Given: 0% rejection probability
        when(tokenizationConfig.getRejectionProbability()).thenReturn(0);

        // Save original CVV
        String originalCvv = validCard.getCvv();
        assertThat(originalCvv).isEqualTo("123");

        // Act
        CreditCard tokenizedCard = tokenizationService.tokenize(validCard);

        // Assert: CVV is cleared
        assertThat(tokenizedCard.getCvv()).isNull();
        assertThat(tokenizedCard.getCardNumber()).isNotNull(); // Card number preserved
        assertThat(tokenizedCard.getCardholderName()).isNotNull(); // Name preserved
    }

    @Test
    @DisplayName("Should handle different card types with valid Luhn")
    void shouldHandleDifferentCardTypesWithValidLuhn() {
        // Given: 0% rejection probability
        when(tokenizationConfig.getRejectionProbability()).thenReturn(0);

        // Mastercard (valid Luhn)
        CreditCard mastercard = CreditCard.builder()
                .cardNumber(new CardNumber("5425233430109903"))
                .cardholderName("Jane Doe")
                .expirationDate("12/25")
                .cvv("456")
                .build();

        // Amex (valid Luhn)
        CreditCard amex = CreditCard.builder()
                .cardNumber(new CardNumber("374245455400126"))
                .cardholderName("Bob Smith")
                .expirationDate("12/25")
                .cvv("7890")
                .build();

        // Act
        CreditCard tokenizedMastercard = tokenizationService.tokenize(mastercard);
        CreditCard tokenizedAmex = tokenizationService.tokenize(amex);

        // Assert
        assertThat(tokenizedMastercard.isTokenized()).isTrue();
        assertThat(tokenizedAmex.isTokenized()).isTrue();
        assertThat(tokenizedMastercard.getToken()).isNotEqualTo(tokenizedAmex.getToken()); // Different tokens
    }

    @Test
    @DisplayName("Should preserve card metadata after tokenization")
    void shouldPreserveCardMetadataAfterTokenization() {
        // Given: 0% rejection probability
        when(tokenizationConfig.getRejectionProbability()).thenReturn(0);

        // Act
        CreditCard tokenizedCard = tokenizationService.tokenize(validCard);

        // Assert: Metadata preserved
        assertThat(tokenizedCard.getCardNumber().value()).isEqualTo("4532015112830366");
        assertThat(tokenizedCard.getCardholderName()).isEqualTo("John Doe");
        assertThat(tokenizedCard.getExpirationDate()).isEqualTo("12/25");
        assertThat(tokenizedCard.getId()).isEqualTo(validCard.getId());
    }

    @Test
    @DisplayName("Should handle probabilistic scenarios correctly")
    void shouldHandleProbabilisticScenariosCorrectly() {
        // Test with 50% probability (may pass or fail, but should handle both)
        when(tokenizationConfig.getRejectionProbability()).thenReturn(50);

        // Run multiple times to test probabilistic behavior
        int successCount = 0;
        int failureCount = 0;
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            CreditCard testCard = CreditCard.builder()
                    .cardNumber(new CardNumber("4532015112830366"))
                    .cardholderName("Test User")
                    .expirationDate("12/25")
                    .cvv("123")
                    .build();

            try {
                tokenizationService.tokenize(testCard);
                successCount++;
            } catch (TokenizationFailedException e) {
                failureCount++;
            }
        }

        // With 50% probability, both successes and failures should occur
        assertThat(successCount).isGreaterThan(0);
        assertThat(failureCount).isGreaterThan(0);
        assertThat(successCount + failureCount).isEqualTo(iterations);
    }
}
