package com.farmatodo.reto_tecnico.domain.exception;

/**
 * Exception thrown when a credit card validation fails.
 * This exception is used for business rule violations related to card validity
 * that go beyond simple tokenization failures.
 *
 * Examples:
 * - Card is blocked or reported stolen
 * - Card type not accepted
 * - Card has exceeded usage limits
 */
public class InvalidCardException extends DomainException {

    private final String cardLastFour;
    private final String reason;

    /**
     * Creates a new InvalidCardException with card details.
     * @param cardLastFour last four digits of the card
     * @param reason the reason the card is invalid
     */
    public InvalidCardException(String cardLastFour, String reason) {
        super(String.format("Invalid credit card (****%s): %s", cardLastFour, reason));
        this.cardLastFour = cardLastFour;
        this.reason = reason;
    }

    /**
     * Creates a new InvalidCardException with simple message.
     * @param message the error message
     */
    public InvalidCardException(String message) {
        super(message);
        this.cardLastFour = null;
        this.reason = message;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public String getReason() {
        return reason;
    }
}
