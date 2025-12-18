package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.Order;

/**
 * Input port for payment processing use case.
 * Defines the contract for processing payments with retry logic.
 * Implementation will handle payment gateway integration and retry mechanisms.
 *
 * Supports two payment flows:
 * 1. Payment with existing token: processPayment(order, paymentToken)
 * 2. Payment with new card: processPayment(order, creditCard) - includes tokenization
 */
public interface ProcessPaymentUseCase {

    /**
     * Processes payment for an order using a credit card.
     * The card will be tokenized before payment processing.
     * Implements retry logic for failed payments according to configuration.
     * @param order the order to process payment for
     * @param creditCard the credit card to charge (will be tokenized)
     * @return PaymentResult with success status and details
     * @throws com.farmatodo.reto_tecnico.domain.exception.PaymentFailedException if payment fails after all retries
     * @throws com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException if tokenization fails
     */
    PaymentResult processPayment(Order order, CreditCard creditCard);

    /**
     * Processes payment for an order using an existing payment token.
     * Skips tokenization step as the token already exists.
     * Implements retry logic for failed payments according to configuration.
     * @param order the order to process payment for
     * @param paymentToken the existing payment token
     * @return PaymentResult with success status and details
     * @throws com.farmatodo.reto_tecnico.domain.exception.PaymentFailedException if payment fails after all retries
     */
    PaymentResult processPaymentWithToken(Order order, String paymentToken);

    /**
     * Payment result data transfer object.
     * Contains information about the payment transaction.
     */
    record PaymentResult(
            boolean success,
            String transactionId,
            String message,
            int attemptsMade
    ) {
        public static PaymentResult success(String transactionId, int attempts) {
            return new PaymentResult(true, transactionId, "Payment successful", attempts);
        }

        public static PaymentResult failure(String message, int attempts) {
            return new PaymentResult(false, null, message, attempts);
        }
    }
}
