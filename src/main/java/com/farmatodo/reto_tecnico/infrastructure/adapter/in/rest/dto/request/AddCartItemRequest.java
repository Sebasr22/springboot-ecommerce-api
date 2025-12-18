package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding an item to the cart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add a product to the shopping cart")
public class AddCartItemRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID who owns the cart", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID customerId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID to add to cart", example = "660e8400-e29b-41d4-a716-446655440000")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product to add", example = "2", minimum = "1")
    private Integer quantity;
}
