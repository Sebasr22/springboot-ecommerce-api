package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a credit card is not found.
 * This can occur when looking up a stored card by ID or by token.
 */
public class CreditCardNotFoundException extends DomainException {

    private final UUID cardId;
    private final String token;

    /**
     * Creates a new CreditCardNotFoundException for card ID lookup.
     * @param cardId the card ID that was not found
     */
    public CreditCardNotFoundException(UUID cardId) {
        super(String.format("Credit card not found with ID: %s", cardId));
        this.cardId = cardId;
        this.token = null;
    }

    /**
     * Creates a new CreditCardNotFoundException for token lookup.
     * @param token the token that was not found
     */
    public CreditCardNotFoundException(String token) {
        super(String.format("Credit card not found for token: %s***", maskToken(token)));
        this.cardId = null;
        this.token = token;
    }

    /**
     * Creates a new CreditCardNotFoundException with custom message.
     * @param message the error message
     * @param isCustomMessage flag to differentiate from token constructor
     */
    public CreditCardNotFoundException(String message, boolean isCustomMessage) {
        super(message);
        this.cardId = null;
        this.token = null;
    }

    public UUID getCardId() {
        return cardId;
    }

    public String getToken() {
        return token;
    }

    /**
     * Masks a token for display purposes.
     */
    private static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 8);
    }
}
