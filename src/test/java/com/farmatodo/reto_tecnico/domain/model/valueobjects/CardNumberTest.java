package com.farmatodo.reto_tecnico.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CardNumber value object.
 * Tests masking and Luhn algorithm validation.
 */
@DisplayName("CardNumber Value Object Tests")
class CardNumberTest {

    @Test
    @DisplayName("Should mask number correctly showing only last 4 digits")
    void shouldMaskNumberCorrectly() {
        // Given: Valid card number
        CardNumber cardNumber = new CardNumber("4532015112830366");

        // When
        String masked = cardNumber.getMasked();

        // Then: Only last 4 digits visible
        assertThat(masked).isEqualTo("************0366");
        assertThat(masked).hasSize(16);
        assertThat(masked).endsWith("0366");
    }

    @Test
    @DisplayName("Should get last four digits correctly")
    void shouldGetLastFourDigitsCorrectly() {
        // Given
        CardNumber cardNumber = new CardNumber("4532015112830366");

        // When
        String lastFour = cardNumber.getLastFourDigits();

        // Then
        assertThat(lastFour).isEqualTo("0366");
    }

    @Test
    @DisplayName("Should get BIN correctly")
    void shouldGetBinCorrectly() {
        // Given: Visa card (starts with 4)
        CardNumber visaCard = new CardNumber("4532015112830366");

        // When
        String bin = visaCard.getBin();

        // Then
        assertThat(bin).isEqualTo("453201");
    }

    @DisplayName("Should validate Luhn algorithm with valid card numbers")
    @ParameterizedTest
    @ValueSource(strings = {
            "4532015112830366", // Visa
            "5425233430109903", // Mastercard
            "374245455400126",  // Amex
            "6011000991300009"  // Discover
    })
    void shouldValidateLuhnAlgorithmWithValidCards(String validCardNumber) {
        // Given: Valid card number
        CardNumber cardNumber = new CardNumber(validCardNumber);

        // When & Then
        assertThat(cardNumber.isValidLuhn()).isTrue();
    }

    @DisplayName("Should validate Luhn algorithm with invalid card numbers")
    @ParameterizedTest
    @ValueSource(strings = {
            "4532015112830367", // Invalid Visa (last digit wrong)
            "5425233430109904", // Invalid Mastercard
            "1234567890123456"  // Invalid (random digits)
    })
    void shouldValidateLuhnAlgorithmWithInvalidCards(String invalidCardNumber) {
        // Given: Invalid card number
        CardNumber cardNumber = new CardNumber(invalidCardNumber);

        // When & Then
        assertThat(cardNumber.isValidLuhn()).isFalse();
    }

    @Test
    @DisplayName("Should normalize card number removing spaces and dashes")
    void shouldNormalizeCardNumberRemovingSpacesAndDashes() {
        // Given: Card number with formatting
        CardNumber cardNumber1 = new CardNumber("4532-0151-1283-0366");
        CardNumber cardNumber2 = new CardNumber("4532 0151 1283 0366");

        // Then: Both normalized to same value
        assertThat(cardNumber1.value()).isEqualTo("4532015112830366");
        assertThat(cardNumber2.value()).isEqualTo("4532015112830366");
        assertThat(cardNumber1).isEqualTo(cardNumber2);
    }

    @Test
    @DisplayName("Should reject null or blank card number")
    void shouldRejectNullOrBlankCardNumber() {
        assertThatThrownBy(() -> new CardNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number cannot be null or blank");

        assertThatThrownBy(() -> new CardNumber(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number cannot be null or blank");

        assertThatThrownBy(() -> new CardNumber("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number cannot be null or blank");
    }

    @Test
    @DisplayName("Should reject card number with less than 13 digits")
    void shouldRejectCardNumberWithLessThan13Digits() {
        assertThatThrownBy(() -> new CardNumber("123456789012"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number must be between 13 and 19 digits");
    }

    @Test
    @DisplayName("Should reject card number with more than 19 digits")
    void shouldRejectCardNumberWithMoreThan19Digits() {
        assertThatThrownBy(() -> new CardNumber("12345678901234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number must be between 13 and 19 digits");
    }

    @Test
    @DisplayName("Should accept card number with 13 digits (minimum)")
    void shouldAcceptCardNumberWith13Digits() {
        // Given: 13-digit card number
        CardNumber cardNumber = new CardNumber("4532015112830");

        // Then
        assertThat(cardNumber.value()).hasSize(13);
        assertThat(cardNumber.getLastFourDigits()).isEqualTo("2830");
    }

    @Test
    @DisplayName("Should accept card number with 19 digits (maximum)")
    void shouldAcceptCardNumberWith19Digits() {
        // Given: 19-digit card number
        CardNumber cardNumber = new CardNumber("1234567890123456789");

        // Then
        assertThat(cardNumber.value()).hasSize(19);
    }

    @Test
    @DisplayName("Should format toString as masked number")
    void shouldFormatToStringAsMaskedNumber() {
        // Given
        CardNumber cardNumber = new CardNumber("4532015112830366");

        // When
        String stringRepresentation = cardNumber.toString();

        // Then
        assertThat(stringRepresentation).isEqualTo("************0366");
    }

    @Test
    @DisplayName("Should reject card number with less than 4 digits")
    void shouldRejectCardNumberWithLessThan4Digits() {
        // When & Then: Card with less than 4 digits not allowed (minimum is 13)
        assertThatThrownBy(() -> new CardNumber("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card number must be between 13 and 19 digits");
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        String originalNumber = "4532015112830366";
        CardNumber cardNumber = new CardNumber(originalNumber);

        // When: Access value
        String value = cardNumber.value();

        // Then: Value is same and cannot be modified externally
        assertThat(value).isEqualTo(originalNumber);

        // Record is immutable by design - no setters exist
        assertThat(cardNumber.value()).isEqualTo(originalNumber);
    }

    @Test
    @DisplayName("Should handle different card lengths for masking")
    void shouldHandleDifferentCardLengthsForMasking() {
        // 13-digit card
        CardNumber card13 = new CardNumber("1234567890123");
        assertThat(card13.getMasked()).isEqualTo("*********0123");

        // 16-digit card
        CardNumber card16 = new CardNumber("1234567890123456");
        assertThat(card16.getMasked()).isEqualTo("************3456");

        // 19-digit card
        CardNumber card19 = new CardNumber("1234567890123456789");
        assertThat(card19.getMasked()).isEqualTo("***************6789");
    }

    @Test
    @DisplayName("Should extract BIN for different card lengths")
    void shouldExtractBinForDifferentCardLengths() {
        // Card with 13 digits (BIN is first 6)
        CardNumber card13 = new CardNumber("1234567890123");
        assertThat(card13.getBin()).isEqualTo("123456");

        // Card with 16 digits (BIN is first 6)
        CardNumber card16 = new CardNumber("4532015112830366");
        assertThat(card16.getBin()).isEqualTo("453201");
    }
}
