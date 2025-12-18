package com.farmatodo.reto_tecnico.domain.model.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Value Object representing a phone number.
 * Immutable and validated at construction time.
 * Only numeric characters are allowed.
 */
public record Phone(
        @NotBlank(message = "Phone cannot be blank")
        @Pattern(
                regexp = "^[0-9]{10,15}$",
                message = "Phone must contain only digits and be between 10 and 15 characters"
        )
        String value
) {
    /**
     * Creates a new Phone value object.
     * @param value the phone number string (digits only)
     * @throws IllegalArgumentException if phone is null, blank, or contains non-numeric characters
     */
    public Phone {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be null or blank");
        }

        // Remove any non-digit characters for normalization
        String normalized = value.replaceAll("[^0-9]", "");

        if (normalized.length() < 10 || normalized.length() > 15) {
            throw new IllegalArgumentException("Phone must be between 10 and 15 digits");
        }

        value = normalized;
    }

    @Override
    public String toString() {
        return value;
    }
}
