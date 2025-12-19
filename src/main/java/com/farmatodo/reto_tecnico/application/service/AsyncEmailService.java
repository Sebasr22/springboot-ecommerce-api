package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronous email service wrapper.
 *
 * CRITICAL ARCHITECTURE DECISION:
 * Email sending MUST be asynchronous to avoid blocking the HTTP response.
 * If SMTP server takes 5 seconds to respond, the user should NOT wait 5 seconds.
 *
 * This service wraps EmailPort calls with @Async to execute in a separate thread pool.
 *
 * Design:
 * - Fire-and-forget pattern (void methods)
 * - Does NOT affect payment transaction commit
 * - Failures are logged but do not throw exceptions back to caller
 * - Uses "taskExecutor" bean configured in AsyncConfig (5-10 thread pool)
 *
 * Thread Safety:
 * - Each method executes in a separate thread from the taskExecutor pool
 * - EmailPort implementation (JavaMailEmailAdapter) is thread-safe
 *
 * Error Handling:
 * - Exceptions are caught and logged
 * - Payment flow continues even if email fails
 * - User experience is not affected by email delivery issues
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailService {

    private final EmailPort emailPort;

    /**
     * Sends payment success email asynchronously.
     *
     * This method returns immediately (Fire-and-Forget).
     * The actual email sending happens in a separate thread from taskExecutor pool.
     *
     * @param order the order that was successfully paid
     * @param transactionId the payment gateway transaction ID
     */
    @Async("taskExecutor")
    public void sendPaymentSuccessEmailAsync(Order order, String transactionId) {
        try {
            String customerEmail = order.getCustomer().getEmail().value();
            String customerName = order.getCustomer().getName();
            String orderId = order.getId().toString();
            String totalAmount = order.getTotalAmount().toString();

            log.debug("[ASYNC] Sending payment success email to: {} for order: {}",
                    customerEmail, orderId);

            emailPort.sendPaymentSuccessEmail(
                    customerEmail,
                    customerName,
                    orderId,
                    totalAmount,
                    transactionId
            );

            log.info("[ASYNC] Payment success email sent to: {} for order: {}",
                    customerEmail, orderId);

        } catch (Exception e) {
            // Log error but don't fail the payment if email fails
            // Payment was already confirmed - email failure should not affect business logic
            log.error("[ASYNC] Failed to send payment success email for order: {}",
                    order.getId(), e);
        }
    }

    /**
     * Sends payment failure email asynchronously.
     *
     * This method returns immediately (Fire-and-Forget).
     * The actual email sending happens in a separate thread from taskExecutor pool.
     *
     * @param order the order that failed payment
     * @param attempts number of payment attempts made
     */
    @Async("taskExecutor")
    public void sendPaymentFailureEmailAsync(Order order, int attempts) {
        try {
            String customerEmail = order.getCustomer().getEmail().value();
            String customerName = order.getCustomer().getName();
            String orderId = order.getId().toString();
            String totalAmount = order.getTotalAmount().toString();

            log.debug("[ASYNC] Sending payment failure email to: {} for order: {}",
                    customerEmail, orderId);

            emailPort.sendPaymentFailureEmail(
                    customerEmail,
                    customerName,
                    orderId,
                    totalAmount,
                    attempts
            );

            log.info("[ASYNC] Payment failure email sent to: {} for order: {}",
                    customerEmail, orderId);

        } catch (Exception e) {
            // Log error but don't propagate exception
            // Order was already marked as failed - email failure is secondary
            log.error("[ASYNC] Failed to send payment failure email for order: {}",
                    order.getId(), e);
        }
    }

    /**
     * Sends a generic email asynchronously.
     *
     * This method returns immediately (Fire-and-Forget).
     * The actual email sending happens in a separate thread from taskExecutor pool.
     *
     * @param to recipient email address
     * @param subject email subject
     * @param body email body (HTML or plain text)
     */
    @Async("taskExecutor")
    public void sendEmailAsync(String to, String subject, String body) {
        try {
            log.debug("[ASYNC] Sending email to: {} with subject: {}", to, subject);

            emailPort.sendEmail(to, subject, body);

            log.info("[ASYNC] Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("[ASYNC] Failed to send email to: {} with subject: {}",
                    to, subject, e);
        }
    }
}
