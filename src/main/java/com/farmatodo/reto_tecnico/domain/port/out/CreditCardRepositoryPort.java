package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for credit card persistence.
 * Defines the contract for credit card data access operations.
 * Implementation will be provided by the infrastructure layer.
 */
public interface CreditCardRepositoryPort {

    /**
     * Saves a credit card (create or update).
     * @param creditCard the credit card to save
     * @return saved credit card
     */
    CreditCard save(CreditCard creditCard);

    /**
     * Finds a credit card by ID.
     * @param id the credit card ID
     * @return Optional containing the credit card if found
     */
    Optional<CreditCard> findById(UUID id);

    /**
     * Finds a credit card by token.
     * @param token the payment token
     * @return Optional containing the credit card if found
     */
    Optional<CreditCard> findByToken(String token);

    /**
     * Finds all credit cards belonging to a customer.
     * @param customerId the customer ID
     * @return list of credit cards for the customer
     */
    List<CreditCard> findByCustomerId(UUID customerId);

    /**
     * Checks if a credit card exists with the given token.
     * @param token the token to check
     * @return true if credit card exists with this token
     */
    boolean existsByToken(String token);
}
