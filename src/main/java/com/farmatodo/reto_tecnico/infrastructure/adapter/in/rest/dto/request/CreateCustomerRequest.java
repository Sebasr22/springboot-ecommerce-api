package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for customer registration.
 * Contains validated customer information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer registration request")
public class CreateCustomerRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    @Schema(description = "Customer full name", example = "Juan Pérez García", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Customer email address (must be unique)", example = "juan.perez@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must contain only digits and be between 10 and 15 characters")
    @Schema(description = "Customer phone number (digits only, 10-15 characters)", example = "573001234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    @Schema(description = "Customer delivery address", example = "Calle 123 #45-67, Bogotá, Colombia", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
}
