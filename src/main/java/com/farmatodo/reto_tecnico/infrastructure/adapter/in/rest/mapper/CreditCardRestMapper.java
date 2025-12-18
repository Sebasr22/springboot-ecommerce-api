package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.CardNumber;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.TokenizeCardRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.TokenResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for CreditCard <-> REST DTO conversion.
 * Handles tokenization requests and responses.
 */
@Mapper(
    componentModel = "spring",
    imports = {CardNumber.class}
)
public interface CreditCardRestMapper {

    /**
     * Converts TokenizeCardRequest to domain CreditCard.
     * @param request tokenization request DTO
     * @return domain credit card
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "cardNumber", expression = "java(new CardNumber(request.getCardNumber()))")
    @Mapping(target = "token", ignore = true) // Set by tokenization service
    CreditCard toDomain(TokenizeCardRequest request);

    /**
     * Converts domain CreditCard to TokenResponse.
     * @param creditCard domain model
     * @return token response DTO
     */
    @Mapping(target = "maskedCardNumber", expression = "java(creditCard.getCardNumber().getMasked())")
    @Mapping(target = "lastFourDigits", expression = "java(creditCard.getCardNumber().getLastFourDigits())")
    TokenResponse toTokenResponse(CreditCard creditCard);
}
