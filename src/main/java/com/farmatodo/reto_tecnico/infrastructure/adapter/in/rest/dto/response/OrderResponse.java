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
 * Response DTO for order information.
 * Contains order details WITHOUT sensitive payment information.
 *
 * SECURITY: Payment tokens and full card numbers are NEVER exposed in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order details response - excludes sensitive payment data")
public class OrderResponse {

    @Schema(description = "Order unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Customer name", example = "Juan Pérez")
    private String customerName;

    @Schema(description = "Customer email", example = "juan.perez@example.com")
    private String customerEmail;

    @Schema(description = "Delivery address for this order", example = "Calle 123, Bogotá")
    private String deliveryAddress;

    @Schema(description = "List of items in the order")
    private List<OrderItemResponse> items;

    @Schema(description = "Total order amount", example = "45000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Order status", example = "PENDING")
    private String status;

    @Schema(description = "Timestamp when order was created", example = "2025-12-16T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when order was last updated", example = "2025-12-16T14:35:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Whether payment has been completed", example = "false")
    private Boolean paymentCompleted;
}
