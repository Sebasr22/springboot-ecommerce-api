package com.farmatodo.reto_tecnico.domain.port.out;

/**
 * Output port for email notifications.
 * Defines the contract for sending emails from the domain layer.
 * Infrastructure adapters will implement this interface.
 */
public interface EmailPort {

    /**
     * Sends an email to the specified recipient.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body (plain text or HTML)
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Sends a payment success notification email.
     *
     * @param to            recipient email address
     * @param customerName  customer's name
     * @param orderId       order ID
     * @param totalAmount   total amount paid
     * @param transactionId payment transaction ID
     */
    void sendPaymentSuccessEmail(
            String to,
            String customerName,
            String orderId,
            String totalAmount,
            String transactionId
    );

    /**
     * Sends a payment failure notification email.
     *
     * @param to           recipient email address
     * @param customerName customer's name
     * @param orderId      order ID
     * @param totalAmount  total amount
     * @param attempts     number of attempts made
     */
    void sendPaymentFailureEmail(
            String to,
            String customerName,
            String orderId,
            String totalAmount,
            int attempts
    );
}
