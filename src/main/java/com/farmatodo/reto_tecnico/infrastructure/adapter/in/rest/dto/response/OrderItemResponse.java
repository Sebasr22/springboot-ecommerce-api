package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for order item information.
 * Contains item details within an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item details")
public class OrderItemResponse {

    @Schema(description = "Order item unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Product unique identifier", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID productId;

    @Schema(description = "Product name", example = "Acetaminofén 500mg")
    private String productName;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price at time of order", example = "12500.00")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal (quantity × unitPrice)", example = "25000.00")
    private BigDecimal subtotal;
}
