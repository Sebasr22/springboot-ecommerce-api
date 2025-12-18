package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CreditCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CreditCardEntity.
 * Provides CRUD operations for credit card persistence.
 *
 * SECURITY NOTE: All queries operate on encrypted tokens.
 * CryptoConverter automatically handles encryption/decryption.
 */
@Repository
public interface CreditCardJpaRepository extends JpaRepository<CreditCardEntity, UUID> {

    /**
     * Finds credit card by last four digits and cardholder name.
     * Used for duplicate checking during tokenization.
     *
     * @param lastFourDigits last 4 digits of card
     * @param cardholderName cardholder name
     * @return Optional containing credit card if found
     */
    @Query("SELECT c FROM CreditCardEntity c WHERE c.lastFourDigits = :lastFour " +
           "AND c.cardholderName = :name")
    Optional<CreditCardEntity> findByLastFourDigitsAndCardholderName(
        @Param("lastFour") String lastFourDigits,
        @Param("name") String cardholderName
    );

    /**
     * Finds credit card by token (encrypted comparison).
     * Token is automatically decrypted by CryptoConverter.
     *
     * @param token the payment token
     * @return Optional containing credit card if found
     */
    Optional<CreditCardEntity> findByToken(String token);

    /**
     * Finds all credit cards belonging to a customer.
     *
     * @param customerId the customer ID
     * @return list of credit cards for the customer
     */
    java.util.List<CreditCardEntity> findByCustomerId(UUID customerId);
}
