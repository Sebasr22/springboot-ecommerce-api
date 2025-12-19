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
@Schema(description = "Request to create a new order. **Typical usage**: provide customerId + items for existing customers. **Alternative**: provide all customer fields (name/email/phone/address) + items for guest checkout (new customers).")
public class CreateOrderRequest {

    @Schema(description = "Existing customer ID (UUID). Use this for registered customers (most common flow).",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID customerId;

    @Size(min = 2, max = 255, message = "Customer name must be between 2 and 255 characters")
    @Schema(description = "Customer full name (only for guest checkout when customerId not provided)",
            example = "Juan Pérez",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            hidden = true)
    private String customerName;

    @Email(message = "Email must be valid")
    @Schema(description = "Customer email address (only for guest checkout when customerId not provided)",
            example = "juan.perez@example.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            hidden = true)
    private String customerEmail;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must contain only digits and be between 10 and 15 characters")
    @Schema(description = "Customer phone number, digits only (only for guest checkout when customerId not provided)",
            example = "573001234567",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            hidden = true)
    private String customerPhone;

    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    @Schema(description = "Customer delivery address (only for guest checkout when customerId not provided)",
            example = "Calle 123 #45-67, Bogotá",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            hidden = true)
    private String customerAddress;

    @Size(min = 5, max = 500, message = "Delivery address must be between 5 and 500 characters")
    @Schema(description = "Delivery address for this specific order (optional). " +
            "Use this to specify a different delivery address than the customer's default address. " +
            "If not provided, the system will use the customer's default address. " +
            "Common use case: sending a gift to another person.",
            example = "Calle 456 #78-90, Medellín",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String deliveryAddress;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @Schema(description = "List of items to order", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;
}
