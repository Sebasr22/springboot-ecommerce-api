package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.Order;

/**
 * Output port for payment gateway operations.
 * Defines the contract for payment processing.
 * Implementation will be provided by the infrastructure layer.
 */
public interface PaymentGatewayPort {

    /**
     * Processes a payment for an order using a payment token.
     * @param order the order to process payment for
     * @param paymentToken the tokenized payment information
     * @return PaymentResult with success status and transaction details
     * @throws com.farmatodo.reto_tecnico.domain.exception.PaymentFailedException if payment fails
     */
    PaymentResult processPayment(Order order, String paymentToken);

    /**
     * Refunds a payment for an order.
     * @param order the order to refund
     * @param transactionId the original transaction ID
     * @return RefundResult with success status
     */
    RefundResult refundPayment(Order order, String transactionId);

    /**
     * Verifies if a payment was successful.
     * @param transactionId the transaction ID to verify
     * @return true if payment is confirmed
     */
    boolean verifyPayment(String transactionId);

    /**
     * Payment result data transfer object.
     */
    record PaymentResult(
            boolean success,
            String transactionId,
            String message
    ) {
        public static PaymentResult success(String transactionId) {
            return new PaymentResult(true, transactionId, "Payment processed successfully");
        }

        public static PaymentResult failure(String message) {
            return new PaymentResult(false, null, message);
        }
    }

    /**
     * Refund result data transfer object.
     */
    record RefundResult(
            boolean success,
            String refundId,
            String message
    ) {
        public static RefundResult success(String refundId) {
            return new RefundResult(true, refundId, "Refund processed successfully");
        }

        public static RefundResult failure(String message) {
            return new RefundResult(false, null, message);
        }
    }
}
