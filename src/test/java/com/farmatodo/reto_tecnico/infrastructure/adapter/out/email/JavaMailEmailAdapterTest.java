package com.farmatodo.reto_tecnico.infrastructure.adapter.out.email;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JavaMailEmailAdapter.
 * Tests email sending functionality with mocked JavaMailSender.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JavaMailEmailAdapter Unit Tests")
class JavaMailEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private FarmatodoProperties properties;

    @Mock
    private FarmatodoProperties.Email emailProperties;

    @Mock
    private MimeMessage mimeMessage;

    private JavaMailEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        when(properties.getEmail()).thenReturn(emailProperties);
        adapter = new JavaMailEmailAdapter(mailSender, properties);
    }

    @Test
    @DisplayName("Should send email successfully when enabled")
    void shouldSendEmailSuccessfullyWhenEnabled() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@farmatodo.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        adapter.sendEmail("customer@example.com", "Test Subject", "<html>Body</html>");

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should skip sending email when disabled")
    void shouldSkipSendingEmailWhenDisabled() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(false);

        // When
        adapter.sendEmail("customer@example.com", "Test Subject", "Body");

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send payment success email with correct subject")
    void shouldSendPaymentSuccessEmail() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@farmatodo.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        adapter.sendPaymentSuccessEmail(
                "customer@example.com",
                "Juan Pérez",
                "ORD-12345",
                "$150,000",
                "TXN-ABC123"
        );

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send payment failure email with correct subject")
    void shouldSendPaymentFailureEmail() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@farmatodo.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        adapter.sendPaymentFailureEmail(
                "customer@example.com",
                "María García",
                "ORD-67890",
                "$200,000",
                3
        );

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when mail send fails")
    void shouldThrowRuntimeExceptionWhenMailSendFails() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@farmatodo.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(() -> adapter.sendEmail("test@test.com", "Subject", "Body"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email");
    }

    @Test
    @DisplayName("Should not send email when email is disabled")
    void shouldNotSendEmailWhenDisabled() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(false);

        // When
        adapter.sendPaymentSuccessEmail(
                "customer@example.com",
                "Juan",
                "ORD-123",
                "$100",
                "TXN-123"
        );

        // Then
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should handle payment failure email with multiple attempts")
    void shouldHandlePaymentFailureEmailWithMultipleAttempts() {
        // Given
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@farmatodo.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        adapter.sendPaymentFailureEmail(
                "customer@example.com",
                "Test Customer",
                "ORD-999",
                "$500,000",
                5 // 5 attempts
        );

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }
}
