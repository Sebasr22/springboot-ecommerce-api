package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a shopping cart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shopping cart with items and total")
public class CartResponse {

    @Schema(description = "Cart ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Customer ID who owns the cart", example = "660e8400-e29b-41d4-a716-446655440000")
    private UUID customerId;

    @Schema(description = "List of items in the cart")
    private List<CartItemResponse> items;

    @Schema(description = "Total amount of all items", example = "75000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Total number of items (sum of quantities)", example = "5")
    private Integer totalItemCount;

    @Schema(description = "Cart creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
