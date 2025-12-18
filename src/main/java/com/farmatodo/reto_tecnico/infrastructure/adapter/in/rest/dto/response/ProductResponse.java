package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for product information.
 * Contains product details for client display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product information response")
public class ProductResponse {

    @Schema(description = "Product unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Product name", example = "Acetaminofén 500mg")
    private String name;

    @Schema(description = "Product description", example = "Analgésico y antipirético de venta libre")
    private String description;

    @Schema(description = "Product price", example = "12500.00")
    private BigDecimal price;

    @Schema(description = "Available stock quantity", example = "150")
    private Integer stock;

    @Schema(description = "Product availability status", example = "true")
    private Boolean inStock;
}
