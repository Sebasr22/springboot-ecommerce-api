package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import com.farmatodo.reto_tecnico.domain.exception.TokenizationFailedException;
import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.port.in.TokenizeCardUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.CreditCardRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

/**
 * Service implementation for credit card tokenization.
 * Simulates tokenization process with configurable failure probability.
 * In production, this would integrate with a real tokenization service (e.g., Stripe, PayU).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenizationServiceImpl implements TokenizeCardUseCase {

    private final FarmatodoProperties properties;
    private final CreditCardRepositoryPort creditCardRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public CreditCard tokenize(CreditCard creditCard) {
        log.info("Tokenizing credit card ending in {} for customer {}",
                creditCard.getCardNumber().getLastFourDigits(),
                creditCard.getCustomerId());

        // Validate card before tokenization
        validateCard(creditCard);

        // Simulate tokenization failure based on configured probability
        if (shouldSimulateFailure()) {
            log.warn("Tokenization failed for card ending in {} (simulated)",
                    creditCard.getCardNumber().getLastFourDigits());
            throw new TokenizationFailedException(
                    "Tokenization service rejected the card. Please try again or use a different card."
            );
        }

        // Generate secure token (in production, this would come from the tokenization service)
        String token = generateToken();
        creditCard.assignToken(token);

        // Clear sensitive data after successful tokenization
        creditCard.clearSensitiveData();

        // Persist the tokenized card to database
        CreditCard savedCard = creditCardRepository.save(creditCard);

        log.info("Successfully tokenized and saved card ending in {}. Token: {}",
                savedCard.getCardNumber().getLastFourDigits(),
                maskToken(token));

        return savedCard;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        // Check format first
        if (!token.startsWith("tok_") || token.length() <= 20) {
            return false;
        }

        // Verify token exists in database
        return creditCardRepository.existsByToken(token);
    }

    /**
     * Validates credit card before tokenization.
     * @param creditCard the card to validate
     * @throws TokenizationFailedException if validation fails
     */
    private void validateCard(CreditCard creditCard) {
        if (creditCard.isExpired()) {
            throw new TokenizationFailedException("Credit card has expired");
        }

        if (!creditCard.getCardNumber().isValidLuhn()) {
            throw new TokenizationFailedException("Invalid credit card number (Luhn check failed)");
        }
    }

    /**
     * Determines if tokenization should fail based on configured probability.
     * @return true if should simulate failure
     */
    private boolean shouldSimulateFailure() {
        int rejectionProbability = properties.getTokenization().getRejectionProbability();
        int randomValue = random.nextInt(100);
        return randomValue < rejectionProbability;
    }

    /**
     * Generates a simulated token.
     * In production, this would come from the tokenization service.
     * @return generated token
     */
    private String generateToken() {
        return "tok_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Masks a token for logging purposes.
     * @param token the token to mask
     * @return masked token
     */
    private String maskToken(String token) {
        if (token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 8) + "***";
    }
}
