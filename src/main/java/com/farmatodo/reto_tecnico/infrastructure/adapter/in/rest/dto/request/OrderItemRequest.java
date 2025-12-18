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
 * Request DTO for creating order items.
 * Represents a single product line in an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item request containing product and quantity")
public class OrderItemRequest {

    @NotNull(message = "Product ID cannot be null")
    @Schema(description = "Product UUID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to order", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
