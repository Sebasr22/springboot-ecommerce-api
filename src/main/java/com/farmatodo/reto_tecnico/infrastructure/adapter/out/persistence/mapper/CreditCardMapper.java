package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CreditCardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for CreditCard <-> CreditCardEntity conversion.
 * Automatically generates implementation at compile time.
 *
 * Handles Value Object conversions:
 * - CardNumber VO <-> Masked String + Last4Digits
 *
 * SECURITY CRITICAL:
 * - CVV is NEVER mapped to entity (remains transient)
 * - Full card number NEVER persisted, only masked version
 * - Token automatically encrypted by CryptoConverter
 */
@Mapper(componentModel = "spring")
public interface CreditCardMapper {

    /**
     * Converts domain CreditCard to JPA CreditCardEntity.
     * SECURITY: CVV is not persisted, full card number becomes masked.
     *
     * @param creditCard domain model
     * @return JPA entity
     */
    @Mapping(target = "cardNumberMasked", expression = "java(creditCard.getCardNumber().getMasked())")
    @Mapping(target = "lastFourDigits", expression = "java(creditCard.getCardNumber().getLastFourDigits())")
    @Mapping(target = "cvv", ignore = true) // NEVER persist CVV
    CreditCardEntity toEntity(CreditCard creditCard);

    /**
     * Converts JPA CreditCardEntity to domain CreditCard.
     * Reconstructs CardNumber from masked value.
     *
     * LIMITATION: Full card number not recoverable (by design for security).
     * CardNumber will only contain masked representation.
     *
     * @param entity JPA entity
     * @return domain model
     */
    @Mapping(target = "cardNumber", expression = "java(reconstructCardNumber(entity))")
    @Mapping(target = "cvv", constant = "") // CVV not stored, return empty
    CreditCard toDomain(CreditCardEntity entity);

    /**
     * Reconstructs CardNumber from entity's last 4 digits.
     * Creates a CardNumber with masked format.
     *
     * NOTE: This is a security feature - we can't recover the full card number.
     * We create a synthetic card number using the last 4 digits.
     *
     * @param entity credit card entity
     * @return CardNumber value object
     */
    default CardNumber reconstructCardNumber(CreditCardEntity entity) {
        // Create a 16-digit synthetic card number with last 4 real digits
        String syntheticNumber = "000000000000" + entity.getLastFourDigits();
        return new CardNumber(syntheticNumber);
    }
}
