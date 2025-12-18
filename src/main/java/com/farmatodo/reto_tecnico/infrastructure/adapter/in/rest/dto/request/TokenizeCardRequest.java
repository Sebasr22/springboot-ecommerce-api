package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for tokenizing a credit card.
 * Contains sensitive card information that will be tokenized.
 *
 * SECURITY: This data is NEVER persisted. Only the generated token is stored (encrypted).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credit card tokenization request - sensitive data not persisted")
public class TokenizeCardRequest {

    @NotBlank(message = "Card number cannot be blank")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be between 13 and 19 digits")
    @Schema(
        description = "Credit card number (digits only, 13-19 characters)",
        example = "4111111111111111",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cardNumber;

    @NotBlank(message = "CVV cannot be blank")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    @Schema(
        description = "Card verification value (3-4 digits)",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cvv;

    @NotBlank(message = "Expiration date cannot be blank")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiration date must be in MM/YY format")
    @Schema(
        description = "Card expiration date in MM/YY format",
        example = "12/25",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String expirationDate;

    @NotBlank(message = "Cardholder name cannot be blank")
    @Pattern(
        regexp = "^[a-zA-Z\\s]{2,255}$",
        message = "Cardholder name must contain only letters and spaces, 2-255 characters"
    )
    @Schema(
        description = "Name as it appears on the card",
        example = "JUAN PEREZ",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cardholderName;
}
