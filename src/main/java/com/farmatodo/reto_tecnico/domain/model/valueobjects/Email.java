package com.farmatodo.reto_tecnico.domain.model.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Value Object representing an email address.
 * Immutable and validated at construction time.
 */
public record Email(
        @NotBlank(message = "Email cannot be blank")
        @jakarta.validation.constraints.Email(message = "Email must be valid")
        @Pattern(
                regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Email format is invalid"
        )
        String value
) {
    /**
     * Creates a new Email value object.
     * @param value the email string
     * @throws IllegalArgumentException if email is null or blank
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        value = value.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
