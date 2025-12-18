package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for payment processing result.
 * Contains payment status and transaction details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment processing result")
public class PaymentResponse {

    @Schema(description = "Order unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID orderId;

    @Schema(description = "Payment success status", example = "true")
    private Boolean success;

    @Schema(description = "Payment gateway transaction ID", example = "txn_1a2b3c4d5e6f")
    private String transactionId;

    @Schema(description = "Number of payment attempts made", example = "1")
    private Integer attempts;

    @Schema(description = "Result message", example = "Payment processed successfully")
    private String message;

    @Schema(description = "Updated order status", example = "PAYMENT_CONFIRMED")
    private String orderStatus;
}
