package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponse DTO.
 * Tests all constructors, getters, setters, and builder.
 */
@DisplayName("ErrorResponse DTO Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("Should create ErrorResponse using builder")
    void shouldCreateUsingBuilder() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> errors = Map.of("email", "must be valid");

        // When
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .path("/api/v1/test")
                .validationErrors(errors)
                .build();

        // Then
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getPath()).isEqualTo("/api/v1/test");
        assertThat(response.getValidationErrors()).containsEntry("email", "must be valid");
    }

    @Test
    @DisplayName("Should create ErrorResponse using no-args constructor")
    void shouldCreateUsingNoArgsConstructor() {
        // When
        ErrorResponse response = new ErrorResponse();

        // Then
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getStatus()).isZero();
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getPath()).isNull();
        assertThat(response.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should create ErrorResponse using all-args constructor")
    void shouldCreateUsingAllArgsConstructor() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> errors = Map.of("name", "must not be blank");

        // When
        ErrorResponse response = new ErrorResponse(
                timestamp,
                404,
                "NOT_FOUND",
                "Resource not found",
                "/api/v1/products/123",
                errors
        );

        // Then
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("NOT_FOUND");
        assertThat(response.getMessage()).isEqualTo("Resource not found");
        assertThat(response.getPath()).isEqualTo("/api/v1/products/123");
        assertThat(response.getValidationErrors()).isEqualTo(errors);
    }

    @Test
    @DisplayName("Should set and get timestamp")
    void shouldSetAndGetTimestamp() {
        // Given
        ErrorResponse response = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.of(2025, 12, 18, 10, 30, 0);

        // When
        response.setTimestamp(timestamp);

        // Then
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should set and get status")
    void shouldSetAndGetStatus() {
        // Given
        ErrorResponse response = new ErrorResponse();

        // When
        response.setStatus(500);

        // Then
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should set and get error code")
    void shouldSetAndGetError() {
        // Given
        ErrorResponse response = new ErrorResponse();

        // When
        response.setError("INTERNAL_SERVER_ERROR");

        // Then
        assertThat(response.getError()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("Should set and get message")
    void shouldSetAndGetMessage() {
        // Given
        ErrorResponse response = new ErrorResponse();

        // When
        response.setMessage("An error occurred");

        // Then
        assertThat(response.getMessage()).isEqualTo("An error occurred");
    }

    @Test
    @DisplayName("Should set and get path")
    void shouldSetAndGetPath() {
        // Given
        ErrorResponse response = new ErrorResponse();

        // When
        response.setPath("/api/v1/orders");

        // Then
        assertThat(response.getPath()).isEqualTo("/api/v1/orders");
    }

    @Test
    @DisplayName("Should set and get validation errors")
    void shouldSetAndGetValidationErrors() {
        // Given
        ErrorResponse response = new ErrorResponse();
        Map<String, String> errors = new HashMap<>();
        errors.put("field1", "error1");
        errors.put("field2", "error2");

        // When
        response.setValidationErrors(errors);

        // Then
        assertThat(response.getValidationErrors()).hasSize(2);
        assertThat(response.getValidationErrors()).containsEntry("field1", "error1");
        assertThat(response.getValidationErrors()).containsEntry("field2", "error2");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEquals() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse response1 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("ERROR")
                .message("Test")
                .path("/test")
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("ERROR")
                .message("Test")
                .path("/test")
                .build();

        // Then
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCode() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse response1 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("ERROR")
                .message("Test")
                .path("/test")
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("ERROR")
                .message("Test")
                .path("/test")
                .build();

        // Then
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .error("VALIDATION_ERROR")
                .message("Test message")
                .build();

        // When
        String result = response.toString();

        // Then
        assertThat(result).contains("400");
        assertThat(result).contains("VALIDATION_ERROR");
        assertThat(result).contains("Test message");
    }

    @Test
    @DisplayName("Should handle null validation errors")
    void shouldHandleNullValidationErrors() {
        // Given
        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .error("ERROR")
                .message("Error")
                .validationErrors(null)
                .build();

        // Then
        assertThat(response.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle empty validation errors")
    void shouldHandleEmptyValidationErrors() {
        // Given
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .error("VALIDATION_ERROR")
                .message("Error")
                .validationErrors(Map.of())
                .build();

        // Then
        assertThat(response.getValidationErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should create response without path for backward compatibility")
    void shouldCreateWithoutPath() {
        // Given & When
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("ERROR")
                .message("Message")
                .build();

        // Then
        assertThat(response.getPath()).isNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
