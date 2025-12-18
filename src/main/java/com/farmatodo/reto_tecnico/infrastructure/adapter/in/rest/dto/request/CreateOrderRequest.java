package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new order.
 * Supports two flows:
 * 1. With existing customer: provide customerId only
 * 2. With new customer: provide all customer data (name, email, phone, address)
 *
 * Validation is handled at service layer to ensure either customerId OR customer data is provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new order. Provide either customerId (for existing customer) OR complete customer data (for new customer)")
public class CreateOrderRequest {

    @Schema(description = "Existing customer ID (UUID). If provided, customer data fields are ignored",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID customerId;

    @Size(min = 2, max = 255, message = "Customer name must be between 2 and 255 characters")
    @Schema(description = "Customer full name (required if customerId not provided)",
            example = "Juan Pérez",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String customerName;

    @Email(message = "Email must be valid")
    @Schema(description = "Customer email address (required if customerId not provided)",
            example = "juan.perez@example.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String customerEmail;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must contain only digits and be between 10 and 15 characters")
    @Schema(description = "Customer phone number, digits only (required if customerId not provided)",
            example = "573001234567",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String customerPhone;

    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    @Schema(description = "Customer delivery address (required if customerId not provided)",
            example = "Calle 123 #45-67, Bogotá",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String customerAddress;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @Schema(description = "List of items to order", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;
}
