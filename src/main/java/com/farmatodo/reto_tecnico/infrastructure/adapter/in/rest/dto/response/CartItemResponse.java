package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for a cart item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shopping cart item details")
public class CartItemResponse {

    @Schema(description = "Cart item ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Product ID", example = "660e8400-e29b-41d4-a716-446655440000")
    private UUID productId;

    @Schema(description = "Product name", example = "Acetaminofén 500mg")
    private String productName;

    @Schema(description = "Quantity in cart", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price", example = "15000.00")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal (unitPrice * quantity)", example = "30000.00")
    private BigDecimal subtotal;
}
