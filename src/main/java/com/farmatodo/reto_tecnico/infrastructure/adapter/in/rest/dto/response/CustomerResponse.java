package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for customer information.
 * Contains customer data including generated ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer information response")
public class CustomerResponse {

    @Schema(description = "Customer unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Customer full name", example = "Juan Pérez García")
    private String name;

    @Schema(description = "Customer email address", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "Customer phone number", example = "573001234567")
    private String phone;

    @Schema(description = "Customer delivery address", example = "Calle 123 #45-67, Bogotá, Colombia")
    private String address;
}
