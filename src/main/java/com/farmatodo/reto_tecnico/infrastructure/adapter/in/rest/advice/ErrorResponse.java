package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response structure for all API errors.
 * Used by GlobalExceptionHandler to return consistent error information.
 *
 * This DTO is referenced in all @ApiResponse annotations for error cases (400, 404, 409, 500, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response structure returned for all API errors")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2025-12-18T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error code identifier", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error message", example = "Validation failed for one or more fields")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders")
    private String path;

    @Schema(description = "Field-level validation errors (only present for validation failures)",
            example = "{\"email\": \"Email must be valid\", \"phone\": \"Phone must contain only digits\"}")
    private Map<String, String> validationErrors;
}
