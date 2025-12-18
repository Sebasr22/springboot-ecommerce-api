package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for cart checkout operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful checkout")
public class CheckoutResponse {

    @Schema(description = "Created order ID", example = "770e8400-e29b-41d4-a716-446655440000")
    private UUID orderId;

    @Schema(description = "Success message", example = "Checkout successful. Order created.")
    private String message;
}
