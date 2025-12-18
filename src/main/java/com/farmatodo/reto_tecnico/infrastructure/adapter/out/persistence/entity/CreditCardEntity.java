package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.converter.CryptoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JPA entity for CreditCard persistence.
 * Maps to 'credit_cards' table in PostgreSQL.
 *
 * SECURITY CRITICAL:
 * - CVV is NEVER persisted (transient field)
 * - Token is encrypted at rest using CryptoConverter (AES-GCM)
 * - Card number stored as masked string (last 4 digits only)
 * - Full card number NEVER stored in database
 */
@Entity
@Table(name = "credit_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Masked card number (e.g., "************1234").
     * Only last 4 digits shown for security.
     * Full card number is NEVER persisted.
     */
    @Column(name = "card_number_masked", nullable = false, length = 50)
    private String cardNumberMasked;

    /**
     * Last 4 digits of card number for display/verification.
     */
    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    /**
     * Card expiration date in MM/YY format.
     */
    @Column(name = "expiration_date", nullable = false, length = 5)
    private String expirationDate;

    /**
     * Encrypted payment token.
     * CRITICAL: Uses CryptoConverter to encrypt/decrypt with AES-GCM.
     * This token is used for actual payment processing.
     */
    @Convert(converter = CryptoConverter.class)
    @Column(name = "token", length = 500)
    private String token;

    /**
     * Cardholder name as appears on card.
     */
    @Column(name = "cardholder_name", nullable = false, length = 255)
    private String cardholderName;

    /**
     * CVV is NEVER persisted.
     * Transient field - only used during tokenization process.
     */
    @Transient
    private String cvv;
}
