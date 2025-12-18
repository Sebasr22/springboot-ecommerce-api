package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CreditCard domain model.
 * Tests builder, factory methods, tokenization, and expiration logic.
 */
@DisplayName("CreditCard Domain Model Tests")
class CreditCardTest {

    private static final String VALID_CARD_NUMBER = "4111111111111111";
    private static final String VALID_CVV = "123";
    private static final String VALID_CARDHOLDER = "Juan Pérez";

    private String getValidExpirationDate() {
        // Return a date 1 year in the future
        YearMonth future = YearMonth.now().plusYears(1);
        return future.format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    private String getExpiredDate() {
        // Return a date 1 year in the past
        YearMonth past = YearMonth.now().minusYears(1);
        return past.format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    @Test
    @DisplayName("Should create credit card using builder")
    void shouldCreateCreditCardUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        CardNumber cardNumber = new CardNumber(VALID_CARD_NUMBER);
        String expirationDate = getValidExpirationDate();

        // When
        CreditCard card = CreditCard.builder()
                .id(id)
                .cardNumber(cardNumber)
                .cvv(VALID_CVV)
                .expirationDate(expirationDate)
                .cardholderName(VALID_CARDHOLDER)
                .build();

        // Then
        assertThat(card.getId()).isEqualTo(id);
        assertThat(card.getCardNumber()).isEqualTo(cardNumber);
        assertThat(card.getCvv()).isEqualTo(VALID_CVV);
        assertThat(card.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(card.getCardholderName()).isEqualTo(VALID_CARDHOLDER);
    }

    @Test
    @DisplayName("Should create credit card using factory method")
    void shouldCreateCreditCardUsingFactoryMethod() {
        // Given
        UUID customerId = UUID.randomUUID();
        CardNumber cardNumber = new CardNumber(VALID_CARD_NUMBER);
        String expirationDate = getValidExpirationDate();

        // When
        CreditCard card = CreditCard.create(customerId, cardNumber, VALID_CVV, expirationDate, VALID_CARDHOLDER);

        // Then
        assertThat(card.getId()).isNotNull();
        assertThat(card.getCustomerId()).isEqualTo(customerId);
        assertThat(card.getCardNumber()).isEqualTo(cardNumber);
        assertThat(card.getCvv()).isEqualTo(VALID_CVV);
        assertThat(card.isTokenized()).isFalse();
    }

    @Test
    @DisplayName("Should assign token successfully")
    void shouldAssignTokenSuccessfully() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .cardNumber(new CardNumber(VALID_CARD_NUMBER))
                .cvv(VALID_CVV)
                .expirationDate(getValidExpirationDate())
                .cardholderName(VALID_CARDHOLDER)
                .build();

        String token = "tok_secure_token_12345";

        // When
        card.assignToken(token);

        // Then
        assertThat(card.getToken()).isEqualTo(token);
        assertThat(card.isTokenized()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when assigning null token")
    void shouldThrowExceptionWhenAssigningNullToken() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .build();

        // When & Then
        assertThatThrownBy(() -> card.assignToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token cannot be null or blank");
    }

    @Test
    @DisplayName("Should throw exception when assigning blank token")
    void shouldThrowExceptionWhenAssigningBlankToken() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .build();

        // When & Then
        assertThatThrownBy(() -> card.assignToken("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token cannot be null or blank");
    }

    @Test
    @DisplayName("Should return false for isTokenized when no token")
    void shouldReturnFalseForIsTokenizedWhenNoToken() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .cardNumber(new CardNumber(VALID_CARD_NUMBER))
                .build();

        // Then
        assertThat(card.isTokenized()).isFalse();
    }

    @Test
    @DisplayName("Should return false for isTokenized when token is blank")
    void shouldReturnFalseForIsTokenizedWhenTokenIsBlank() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .token("")
                .build();

        // Then
        assertThat(card.isTokenized()).isFalse();
    }

    @Test
    @DisplayName("Should detect expired card")
    void shouldDetectExpiredCard() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .expirationDate(getExpiredDate())
                .build();

        // Then
        assertThat(card.isExpired()).isTrue();
    }

    @Test
    @DisplayName("Should detect non-expired card")
    void shouldDetectNonExpiredCard() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .expirationDate(getValidExpirationDate())
                .build();

        // Then
        assertThat(card.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should return true for isExpired when date format is invalid")
    void shouldReturnTrueForIsExpiredWhenDateFormatIsInvalid() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .expirationDate("invalid-date")
                .build();

        // Then - invalid dates considered expired for safety
        assertThat(card.isExpired()).isTrue();
    }

    @Test
    @DisplayName("Should clear sensitive data")
    void shouldClearSensitiveData() {
        // Given
        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID())
                .cvv(VALID_CVV)
                .build();

        // When
        card.clearSensitiveData();

        // Then
        assertThat(card.getCvv()).isNull();
    }

    @Test
    @DisplayName("Should return masked card info")
    void shouldReturnMaskedCardInfo() {
        // Given
        CardNumber cardNumber = new CardNumber(VALID_CARD_NUMBER);
        String expirationDate = "12/25";
        CreditCard card = CreditCard.builder()
                .cardNumber(cardNumber)
                .expirationDate(expirationDate)
                .build();

        // When
        String maskedInfo = card.getMaskedCardInfo();

        // Then
        assertThat(maskedInfo).contains("1111"); // Last 4 digits
        assertThat(maskedInfo).contains("12/25");
        assertThat(maskedInfo).contains("****"); // Masked portion
    }

    @Test
    @DisplayName("Should create credit card using no-args constructor")
    void shouldCreateCreditCardUsingNoArgsConstructor() {
        // When
        CreditCard card = new CreditCard();

        // Then
        assertThat(card.getId()).isNull();
        assertThat(card.getToken()).isNull();
    }

    @Test
    @DisplayName("Should create credit card using all-args constructor")
    void shouldCreateCreditCardUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CardNumber cardNumber = new CardNumber(VALID_CARD_NUMBER);

        // When
        CreditCard card = new CreditCard(id, customerId, cardNumber, VALID_CVV, "12/26", "tok_123", VALID_CARDHOLDER);

        // Then
        assertThat(card.getId()).isEqualTo(id);
        assertThat(card.getCustomerId()).isEqualTo(customerId);
        assertThat(card.getCardNumber()).isEqualTo(cardNumber);
        assertThat(card.getToken()).isEqualTo("tok_123");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        CardNumber cardNumber = new CardNumber(VALID_CARD_NUMBER);

        CreditCard card1 = CreditCard.builder()
                .id(id)
                .cardNumber(cardNumber)
                .cvv(VALID_CVV)
                .expirationDate("12/26")
                .cardholderName(VALID_CARDHOLDER)
                .build();

        CreditCard card2 = CreditCard.builder()
                .id(id)
                .cardNumber(cardNumber)
                .cvv(VALID_CVV)
                .expirationDate("12/26")
                .cardholderName(VALID_CARDHOLDER)
                .build();

        // Then
        assertThat(card1).isEqualTo(card2);
        assertThat(card1.hashCode()).isEqualTo(card2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        CreditCard card = CreditCard.builder()
                .cardholderName("Test Holder")
                .expirationDate("01/27")
                .build();

        // When
        String result = card.toString();

        // Then
        assertThat(result).contains("Test Holder");
    }

    @Test
    @DisplayName("Should handle 4 digit CVV")
    void shouldHandleFourDigitCvv() {
        // Given - AMEX cards have 4 digit CVV
        CreditCard card = CreditCard.builder()
                .cvv("1234")
                .build();

        // Then
        assertThat(card.getCvv()).isEqualTo("1234");
    }
}
