package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for processing payment on an order.
 * Supports two payment flows:
 * 1. Payment with existing token: Provide paymentToken only
 * 2. Payment with new card: Provide creditCard (will be tokenized)
 *
 * Validation is handled at service layer to ensure exactly one is provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment processing request. **Typical usage**: provide paymentToken (if card was tokenized previously). **Alternative**: provide creditCard object for new card (will be tokenized automatically).")
public class ProcessPaymentRequest {

    @Schema(
        description = "Existing payment token from previous tokenization (most common flow). Use this if customer already tokenized their card.",
        example = "tok_3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String paymentToken;

    @Valid
    @Schema(
        description = "Credit card details for payment (only if paymentToken not provided). Card will be tokenized automatically before payment processing.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        hidden = true
    )
    private TokenizeCardRequest creditCard;
}
