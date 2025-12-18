package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * CreditCard domain entity.
 * Represents a credit card with tokenization support.
 * Pure domain model without persistence annotations.
 * Note: CVV should never be persisted, only used for tokenization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {

    /**
     * Unique identifier for the credit card record.
     */
    private UUID id;

    /**
     * ID of the customer who owns this credit card.
     * Required for referential integrity with customers table.
     */
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

    /**
     * Card number (Value Object with masking).
     */
    @NotNull(message = "Card number cannot be null")
    private CardNumber cardNumber;

    /**
     * Card Verification Value (CVV).
     * IMPORTANT: This should NEVER be persisted, only used during tokenization.
     */
    @NotBlank(message = "CVV cannot be blank")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    /**
     * Card expiration date (MM/YY format).
     */
    @NotBlank(message = "Expiration date cannot be blank")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiration date must be in MM/YY format")
    private String expirationDate;

    /**
     * Tokenized card reference (generated after successful tokenization).
     * This is the secure token used for payments instead of the actual card number.
     */
    private String token;

    /**
     * Cardholder name.
     */
    @NotBlank(message = "Cardholder name cannot be blank")
    private String cardholderName;

    /**
     * Creates a new credit card instance for tokenization.
     * @param customerId ID of the customer who owns this card
     * @param cardNumber card number
     * @param cvv CVV code
     * @param expirationDate expiration date in MM/YY format
     * @param cardholderName cardholder name
     * @return new CreditCard instance
     */
    public static CreditCard create(UUID customerId, CardNumber cardNumber, String cvv, String expirationDate, String cardholderName) {
        return CreditCard.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .cardNumber(cardNumber)
                .cvv(cvv)
                .expirationDate(expirationDate)
                .cardholderName(cardholderName)
                .build();
    }

    /**
     * Assigns a token to this credit card after successful tokenization.
     * @param token the generated token
     */
    public void assignToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }
        this.token = token;
    }

    /**
     * Checks if card has been tokenized.
     * @return true if token is assigned
     */
    public boolean isTokenized() {
        return this.token != null && !this.token.isBlank();
    }

    /**
     * Checks if the card is expired.
     * @return true if card has expired
     */
    public boolean isExpired() {
        try {
            YearMonth expiration = parseExpirationDate();
            YearMonth now = YearMonth.now();
            return expiration.isBefore(now);
        } catch (Exception e) {
            return true; // If we can't parse, consider it expired for safety
        }
    }

    /**
     * Parses the expiration date string into YearMonth.
     * @return YearMonth representation of expiration date
     */
    private YearMonth parseExpirationDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        return YearMonth.parse(expirationDate, formatter);
    }

    /**
     * Clears sensitive data (CVV).
     * Should be called after tokenization is complete.
     */
    public void clearSensitiveData() {
        this.cvv = null;
    }

    /**
     * Returns masked card information for display.
     * @return masked card string
     */
    public String getMaskedCardInfo() {
        return String.format("%s - Exp: %s", cardNumber.getMasked(), expirationDate);
    }
}
