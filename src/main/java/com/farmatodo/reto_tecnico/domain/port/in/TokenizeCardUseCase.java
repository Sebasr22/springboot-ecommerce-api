package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;

/**
 * Input port for credit card tokenization use case.
 * Defines the contract for tokenizing credit card information.
 * Implementation will handle the actual tokenization logic.
 */
public interface TokenizeCardUseCase {

    /**
     * Tokenizes a credit card.
     * Replaces sensitive card information with a secure token.
     * @param creditCard the credit card to tokenize
     * @return tokenized credit card with assigned token
     * @throws com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException if tokenization fails
     */
    CreditCard tokenize(CreditCard creditCard);

    /**
     * Validates if a token is still valid.
     * @param token the token to validate
     * @return true if token is valid and can be used for payments
     */
    boolean validateToken(String token);
}
