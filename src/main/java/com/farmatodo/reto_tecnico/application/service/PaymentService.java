package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import com.farmatodo.reto_tecnico.domain.exception.PaymentFailedException;
import com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException;
import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.port.in.ProcessPaymentUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.TokenizeCardUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.EmailPort;
import com.farmatodo.reto_tecnico.domain.port.out.PaymentGatewayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service implementation for payment processing with retry logic.
 * This is the most critical service in the application.
 * Handles tokenization, payment processing, and retry mechanism as per business requirements.
 *
 * REFACTORED: Removed @Transactional from main method to avoid holding DB connections
 * during Thread.sleep(). Individual transactions are managed by PaymentTransactionService.
 *
 * NOTE: Payment gateway simulation is done internally (attemptPayment method) rather than
 * delegating to an external PaymentGatewayPort adapter. This keeps the simulation logic
 * centralized and avoids unnecessary abstraction layers for a simulated system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements ProcessPaymentUseCase {

    private final TokenizeCardUseCase tokenizationService;
    private final PaymentTransactionService transactionService;
    private final FarmatodoProperties properties;
    private final EmailPort emailPort;

    @Override
    public PaymentResult processPayment(Order order, CreditCard creditCard) {
        log.info("Processing payment for order: {} with total: {} (with credit card tokenization)",
                order.getId(), order.getTotalAmount());

        try {
            // Step 1: Tokenize credit card
            CreditCard tokenizedCard = tokenizeCard(creditCard);
            String paymentToken = tokenizedCard.getToken();

            // Assign token to order in separate transaction
            transactionService.assignTokenAndSave(order, paymentToken);

            // Step 2: Process payment with retry logic (NO @Transactional here)
            return processPaymentWithRetry(order, paymentToken);

        } catch (TokenizationFailedException e) {
            log.error("Tokenization failed for order: {}", order.getId(), e);
            transactionService.failPaymentAndSave(order);
            throw new PaymentFailedException(
                    "Unable to process payment: tokenization failed - " + e.getMessage(), e
            );
        } catch (Exception e) {
            log.error("Unexpected error processing payment for order: {}", order.getId(), e);
            transactionService.failPaymentAndSave(order);
            throw new PaymentFailedException(
                    "Unexpected error processing payment: " + e.getMessage(), e
            );
        }
    }

    @Override
    public PaymentResult processPaymentWithToken(Order order, String paymentToken) {
        log.info("Processing payment for order: {} with total: {} (using existing token)",
                order.getId(), order.getTotalAmount());

        try {
            // Validate token is provided
            if (paymentToken == null || paymentToken.isBlank()) {
                throw new IllegalArgumentException("Payment token cannot be null or blank");
            }

            // Assign token to order in separate transaction
            transactionService.assignTokenAndSave(order, paymentToken);

            // Process payment with retry logic (NO @Transactional here)
            return processPaymentWithRetry(order, paymentToken);

        } catch (Exception e) {
            log.error("Unexpected error processing payment with token for order: {}", order.getId(), e);
            transactionService.failPaymentAndSave(order);
            throw new PaymentFailedException(
                    "Unexpected error processing payment: " + e.getMessage(), e
            );
        }
    }

    /**
     * Tokenizes the credit card before payment processing.
     * @param creditCard the credit card to tokenize
     * @return tokenized credit card
     * @throws TokenizationFailedException if tokenization fails
     */
    private CreditCard tokenizeCard(CreditCard creditCard) {
        log.info("Tokenizing credit card ending in {}",
                creditCard.getCardNumber().getLastFourDigits());
        return tokenizationService.tokenize(creditCard);
    }

    /**
     * Processes payment with retry logic.
     * Implements manual retry mechanism for better control over the process.
     * NO @Transactional here - each save is in its own transaction via transactionService.
     *
     * @param order the order to process payment for
     * @param paymentToken the tokenized payment information
     * @return PaymentResult with success status
     * @throws PaymentFailedException if payment fails after all retries
     */
    private PaymentResult processPaymentWithRetry(Order order, String paymentToken) {
        int maxRetries = properties.getPayment().getMaxRetries();
        long retryDelay = properties.getPayment().getRetryDelayMillis();
        int attempt = 0;

        log.info("Starting payment processing with max {} retries", maxRetries);

        while (attempt < maxRetries) {
            attempt++;
            log.info("Payment attempt {}/{} for order: {}", attempt, maxRetries, order.getId());

            try {
                // Attempt payment
                PaymentGatewayPort.PaymentResult gatewayResult =
                        attemptPayment(order, paymentToken, attempt);

                if (gatewayResult.success()) {
                    // Payment successful - save in separate transaction
                    transactionService.confirmPaymentAndSave(order);

                    log.info("Payment successful for order: {} on attempt {}/{}",
                            order.getId(), attempt, maxRetries);

                    // Send payment success email
                    sendPaymentSuccessEmail(order, gatewayResult.transactionId());

                    return PaymentResult.success(gatewayResult.transactionId(), attempt);
                }

                // Payment failed, but we have more retries
                log.warn("Payment attempt {}/{} failed for order: {}. Reason: {}",
                        attempt, maxRetries, order.getId(), gatewayResult.message());

                // Wait before retry (except on last attempt) - DB connection released during sleep
                if (attempt < maxRetries) {
                    waitBeforeRetry(retryDelay);
                }

            } catch (Exception e) {
                log.error("Exception during payment attempt {}/{} for order: {}",
                        attempt, maxRetries, order.getId(), e);

                // Wait before retry (except on last attempt)
                if (attempt < maxRetries) {
                    waitBeforeRetry(retryDelay);
                }
            }
        }

        // All retries exhausted - save failure in separate transaction
        log.error("Payment failed for order: {} after {} attempts", order.getId(), maxRetries);
        transactionService.failPaymentAndSave(order);

        // Send payment failure email
        sendPaymentFailureEmail(order, maxRetries);

        throw new PaymentFailedException(
                order.getId(),
                "Payment rejected after " + maxRetries + " attempts",
                maxRetries
        );
    }

    /**
     * Attempts a single payment transaction.
     * Simulates payment processing with configurable failure probability.
     *
     * @param order the order
     * @param paymentToken the payment token
     * @param attemptNumber the current attempt number
     * @return PaymentResult from gateway
     */
    private PaymentGatewayPort.PaymentResult attemptPayment(
            Order order,
            String paymentToken,
            int attemptNumber
    ) {
        // Simulate payment gateway call with configurable rejection probability
        boolean shouldFail = shouldSimulatePaymentFailure();

        if (shouldFail) {
            log.debug("Simulating payment failure for order: {} (attempt {})",
                    order.getId(), attemptNumber);
            return PaymentGatewayPort.PaymentResult.failure(
                    "Payment rejected by gateway (simulation)"
            );
        }

        // Generate transaction ID
        String transactionId = "txn_" + UUID.randomUUID().toString().replace("-", "");

        log.debug("Simulating successful payment for order: {} (attempt {})",
                order.getId(), attemptNumber);

        return PaymentGatewayPort.PaymentResult.success(transactionId);
    }

    /**
     * Determines if payment should fail based on configured probability.
     * Uses ThreadLocalRandom to avoid contention under concurrent load.
     * @return true if should simulate failure
     */
    private boolean shouldSimulatePaymentFailure() {
        int rejectionProbability = properties.getPayment().getRejectionProbability();
        int randomValue = ThreadLocalRandom.current().nextInt(100);
        return randomValue < rejectionProbability;
    }

    /**
     * Waits before retry attempt.
     * @param delayMillis delay in milliseconds
     */
    private void waitBeforeRetry(long delayMillis) {
        try {
            log.debug("Waiting {}ms before next retry attempt", delayMillis);
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Retry wait interrupted", e);
        }
    }

    /**
     * Sends payment success email to customer.
     * @param order the order
     * @param transactionId the transaction ID
     */
    private void sendPaymentSuccessEmail(Order order, String transactionId) {
        try {
            String customerEmail = order.getCustomer().getEmail().value();
            String customerName = order.getCustomer().getName();
            String orderId = order.getId().toString();
            String totalAmount = order.getTotalAmount().toString();

            emailPort.sendPaymentSuccessEmail(
                    customerEmail,
                    customerName,
                    orderId,
                    totalAmount,
                    transactionId
            );

            log.info("Payment success email sent to: {} for order: {}", customerEmail, orderId);
        } catch (Exception e) {
            // Log error but don't fail the payment if email fails
            log.error("Failed to send payment success email for order: {}", order.getId(), e);
        }
    }

    /**
     * Sends payment failure email to customer.
     * @param order the order
     * @param attempts number of attempts made
     */
    private void sendPaymentFailureEmail(Order order, int attempts) {
        try {
            String customerEmail = order.getCustomer().getEmail().value();
            String customerName = order.getCustomer().getName();
            String orderId = order.getId().toString();
            String totalAmount = order.getTotalAmount().toString();

            emailPort.sendPaymentFailureEmail(
                    customerEmail,
                    customerName,
                    orderId,
                    totalAmount,
                    attempts
            );

            log.info("Payment failure email sent to: {} for order: {}", customerEmail, orderId);
        } catch (Exception e) {
            // Log error but don't fail the entire process if email fails
            log.error("Failed to send payment failure email for order: {}", order.getId(), e);
        }
    }
}
