package com.farmatodo.reto_tecnico.domain.model.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Value Object representing a credit card number.
 * Immutable and provides masking functionality for security.
 * Only shows last 4 digits when displayed.
 */
public record CardNumber(
        @NotBlank(message = "Card number cannot be blank")
        @Pattern(
                regexp = "^[0-9]{13,19}$",
                message = "Card number must be between 13 and 19 digits"
        )
        String value
) {
    /**
     * Creates a new CardNumber value object.
     * @param value the card number string (digits only)
     * @throws IllegalArgumentException if card number is invalid
     */
    public CardNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Card number cannot be null or blank");
        }

        // Remove any non-digit characters for normalization
        String normalized = value.replaceAll("[^0-9]", "");

        if (normalized.length() < 13 || normalized.length() > 19) {
            throw new IllegalArgumentException("Card number must be between 13 and 19 digits");
        }

        value = normalized;
    }

    /**
     * Returns a masked version of the card number.
     * Shows only the last 4 digits, e.g., "************1234"
     * @return masked card number
     */
    public String getMasked() {
        if (value.length() < 4) {
            return "****";
        }
        int maskLength = value.length() - 4;
        String lastFour = value.substring(value.length() - 4);
        return "*".repeat(maskLength) + lastFour;
    }

    /**
     * Returns the last 4 digits of the card number.
     * @return last 4 digits
     */
    public String getLastFourDigits() {
        if (value.length() < 4) {
            return value;
        }
        return value.substring(value.length() - 4);
    }

    /**
     * Returns the first 6 digits (BIN/IIN) of the card.
     * Useful for identifying card issuer.
     * @return first 6 digits, or less if card is shorter
     */
    public String getBin() {
        return value.length() >= 6 ? value.substring(0, 6) : value;
    }

    /**
     * Validates card number using Luhn algorithm.
     * @return true if card number passes Luhn check
     */
    public boolean isValidLuhn() {
        int sum = 0;
        boolean alternate = false;

        for (int i = value.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(value.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    @Override
    public String toString() {
        return getMasked();
    }
}
