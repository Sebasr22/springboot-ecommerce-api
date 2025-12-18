package com.farmatodo.reto_tecnico.infrastructure.adapter.out.email;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import com.farmatodo.reto_tecnico.domain.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email adapter implementation using JavaMailSender.
 * Sends emails through SMTP (MailHog in development, real SMTP in production).
 * Implements the EmailPort defined in the domain layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JavaMailEmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final FarmatodoProperties properties;

    @Override
    public void sendEmail(String to, String subject, String body) {
        // Check if email sending is enabled
        if (!properties.getEmail().isEnabled()) {
            log.info("Email sending is disabled. Skipping email to: {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(properties.getEmail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML content

            mailSender.send(message);

            log.info("Email sent successfully to: {} with subject: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to create email message for: {}", to, e);
            throw new RuntimeException("Failed to create email message", e);
        } catch (MailException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendPaymentSuccessEmail(
            String to,
            String customerName,
            String orderId,
            String totalAmount,
            String transactionId
    ) {
        log.info("Sending payment success email to: {} for order: {}", to, orderId);

        String subject = "✅ Pago Confirmado - Farmatodo";
        String body = buildPaymentSuccessEmailBody(customerName, orderId, totalAmount, transactionId);

        sendEmail(to, subject, body);
    }

    @Override
    public void sendPaymentFailureEmail(
            String to,
            String customerName,
            String orderId,
            String totalAmount,
            int attempts
    ) {
        log.info("Sending payment failure email to: {} for order: {}", to, orderId);

        String subject = "❌ Pago Rechazado - Farmatodo";
        String body = buildPaymentFailureEmailBody(customerName, orderId, totalAmount, attempts);

        sendEmail(to, subject, body);
    }

    /**
     * Builds HTML email body for payment success notification.
     */
    private String buildPaymentSuccessEmailBody(
            String customerName,
            String orderId,
            String totalAmount,
            String transactionId
    ) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                        .detail { margin: 10px 0; }
                        .label { font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ Pago Confirmado</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            <p>Tu pago ha sido procesado exitosamente. Aquí están los detalles de tu transacción:</p>

                            <div class="detail">
                                <span class="label">ID de Orden:</span> %s
                            </div>
                            <div class="detail">
                                <span class="label">Monto Total:</span> %s
                            </div>
                            <div class="detail">
                                <span class="label">ID de Transacción:</span> %s
                            </div>

                            <p>Gracias por tu compra en Farmatodo.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un correo automático. Por favor no responder.</p>
                            <p>&copy; 2025 Farmatodo. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                customerName,
                orderId,
                totalAmount,
                transactionId
        );
    }

    /**
     * Builds HTML email body for payment failure notification.
     */
    private String buildPaymentFailureEmailBody(
            String customerName,
            String orderId,
            String totalAmount,
            int attempts
    ) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                        .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                        .detail { margin: 10px 0; }
                        .label { font-weight: bold; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 10px; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>❌ Pago Rechazado</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>

                            <div class="warning">
                                <p><strong>⚠️ Tu pago no pudo ser procesado</strong></p>
                                <p>Intentamos procesar tu pago %d veces sin éxito.</p>
                            </div>

                            <p>Detalles de la orden:</p>

                            <div class="detail">
                                <span class="label">ID de Orden:</span> %s
                            </div>
                            <div class="detail">
                                <span class="label">Monto Total:</span> %s
                            </div>
                            <div class="detail">
                                <span class="label">Intentos realizados:</span> %d
                            </div>

                            <p><strong>¿Qué puedes hacer?</strong></p>
                            <ul>
                                <li>Verifica que tu tarjeta tenga fondos suficientes</li>
                                <li>Contacta a tu banco para confirmar que la tarjeta esté activa</li>
                                <li>Intenta nuevamente con otra forma de pago</li>
                            </ul>

                            <p>Si necesitas ayuda, por favor contacta a nuestro servicio al cliente.</p>
                        </div>
                        <div class="footer">
                            <p>Este es un correo automático. Por favor no responder.</p>
                            <p>&copy; 2025 Farmatodo. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                customerName,
                attempts,
                orderId,
                totalAmount,
                attempts
        );
    }
}
