package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for card tokenization.
 * Returns masked card information and token reference.
 *
 * SECURITY: Full card number NEVER returned. Only masked version.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Card tokenization response with masked card information")
public class TokenResponse {

    @Schema(description = "Generated payment token", example = "tok_1a2b3c4d5e6f7g8h")
    private String token;

    @Schema(description = "Masked card number", example = "************1234")
    private String maskedCardNumber;

    @Schema(description = "Last 4 digits of card", example = "1234")
    private String lastFourDigits;

    @Schema(description = "Card expiration date", example = "12/25")
    private String expirationDate;

    @Schema(description = "Cardholder name", example = "JUAN PEREZ")
    private String cardholderName;
}
