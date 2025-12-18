package com.farmatodo.reto_tecnico.application.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Farmatodo business rules.
 * Maps properties from application.properties with prefix "farmatodo".
 * Provides type-safe access to configuration values with validation.
 */
@Configuration
@ConfigurationProperties(prefix = "farmatodo")
@Validated
@Data
public class FarmatodoProperties {

    private Tokenization tokenization = new Tokenization();
    private Payment payment = new Payment();
    private Product product = new Product();
    private Encryption encryption = new Encryption();
    private Email email = new Email();

    /**
     * Tokenization configuration properties.
     */
    @Data
    public static class Tokenization {
        /**
         * Probability (0-100) of tokenization rejection for simulation.
         */
        @Min(0)
        @Max(100)
        private int rejectionProbability = 10;
    }

    /**
     * Payment configuration properties.
     */
    @Data
    public static class Payment {
        /**
         * Probability (0-100) of payment rejection for simulation.
         */
        @Min(0)
        @Max(100)
        private int rejectionProbability = 20;

        /**
         * Maximum number of payment retry attempts.
         */
        @Min(1)
        @Max(10)
        private int maxRetries = 3;

        /**
         * Delay in milliseconds between retry attempts.
         */
        @Min(100)
        @Max(10000)
        private long retryDelayMillis = 1000;
    }

    /**
     * Product configuration properties.
     */
    @Data
    public static class Product {
        /**
         * Minimum stock threshold for low stock alerts.
         */
        @Min(0)
        private int minStockThreshold = 1;
    }

    /**
     * Encryption configuration properties.
     */
    @Data
    public static class Encryption {
        /**
         * Encryption algorithm to use.
         */
        private String algorithm = "AES";

        /**
         * Encryption key (should be provided via environment variable in production).
         */
        private String key;
    }

    /**
     * Email configuration properties.
     */
    @Data
    public static class Email {
        /**
         * From email address for outgoing emails.
         */
        private String from = "noreply@farmatodo.com";

        /**
         * Enable or disable email sending (useful for testing).
         */
        private boolean enabled = true;
    }
}
