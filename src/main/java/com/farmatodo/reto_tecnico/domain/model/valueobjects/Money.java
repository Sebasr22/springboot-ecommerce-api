package com.farmatodo.reto_tecnico.domain.model.valueobjects;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing a monetary amount.
 * Immutable and validated to ensure non-negative values.
 * Uses BigDecimal for precise decimal arithmetic.
 */
public record Money(
        @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "Amount cannot be negative")
        BigDecimal amount
) {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Creates a new Money value object.
     * @param amount the monetary amount
     * @throws IllegalArgumentException if amount is null or negative
     */
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        // Normalize to 2 decimal places
        amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Creates a Money object from a double value.
     * @param value the double value
     * @return Money instance
     */
    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value));
    }

    /**
     * Creates a Money object from a BigDecimal.
     * @param amount the BigDecimal amount
     * @return Money instance
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * Creates a zero Money object.
     * @return Money instance with zero amount
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /**
     * Adds two Money objects.
     * @param other the other Money to add
     * @return new Money with the sum
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * Subtracts another Money from this one.
     * @param other the Money to subtract
     * @return new Money with the difference
     */
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    /**
     * Multiplies this Money by a quantity.
     * @param multiplier the multiplier
     * @return new Money with the product
     */
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    /**
     * Multiplies this Money by a BigDecimal.
     * @param multiplier the multiplier
     * @return new Money with the product
     */
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier));
    }

    /**
     * Checks if this Money is greater than another.
     * @param other the other Money
     * @return true if this is greater than other
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this Money is less than another.
     * @param other the other Money
     * @return true if this is less than other
     */
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks if this Money is zero.
     * @return true if amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
