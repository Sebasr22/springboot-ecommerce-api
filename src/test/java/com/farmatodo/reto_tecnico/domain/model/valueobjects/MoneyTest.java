package com.farmatodo.reto_tecnico.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Money value object.
 * Tests arithmetic operations and validations.
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should not accept negative value")
    void shouldNotAcceptNegativeValue() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be negative");
    }

    @Test
    @DisplayName("Should not accept null value")
    void shouldNotAcceptNullValue() {
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be null");
    }

    @Test
    @DisplayName("Should add and subtract correctly")
    void shouldAddAndSubtractCorrectly() {
        // Given
        Money money1 = new Money(new BigDecimal("100.00"));
        Money money2 = new Money(new BigDecimal("50.00"));

        // When: Add
        Money sum = money1.add(money2);

        // Then
        assertThat(sum.amount()).isEqualByComparingTo(new BigDecimal("150.00"));

        // When: Subtract
        Money difference = money1.subtract(money2);

        // Then
        assertThat(difference.amount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should multiply correctly by integer")
    void shouldMultiplyCorrectlyByInteger() {
        // Given
        Money money = new Money(new BigDecimal("25.00"));

        // When
        Money result = money.multiply(4);

        // Then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should multiply correctly by BigDecimal")
    void shouldMultiplyCorrectlyByBigDecimal() {
        // Given
        Money money = new Money(new BigDecimal("100.00"));

        // When
        Money result = money.multiply(new BigDecimal("1.5"));

        // Then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should create zero Money")
    void shouldCreateZeroMoney() {
        // When
        Money zero = Money.zero();

        // Then
        assertThat(zero.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(zero.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should create Money from double")
    void shouldCreateMoneyFromDouble() {
        // When
        Money money = Money.of(99.99);

        // Then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should compare Money values correctly")
    void shouldCompareMoneyValuesCorrectly() {
        // Given
        Money larger = new Money(new BigDecimal("100.00"));
        Money smaller = new Money(new BigDecimal("50.00"));

        // Then
        assertThat(larger.isGreaterThan(smaller)).isTrue();
        assertThat(smaller.isLessThan(larger)).isTrue();
        assertThat(larger.isLessThan(smaller)).isFalse();
        assertThat(smaller.isGreaterThan(larger)).isFalse();
    }

    @Test
    @DisplayName("Should normalize amounts to 2 decimal places")
    void shouldNormalizeAmountsTo2DecimalPlaces() {
        // Given: Amount with more than 2 decimals
        Money money = new Money(new BigDecimal("10.999"));

        // Then: Rounded to 2 decimal places (HALF_UP)
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("11.00"));
    }

    @Test
    @DisplayName("Should detect zero correctly")
    void shouldDetectZeroCorrectly() {
        // Given
        Money zero = Money.zero();
        Money nonZero = new Money(new BigDecimal("0.01"));

        // Then
        assertThat(zero.isZero()).isTrue();
        assertThat(nonZero.isZero()).isFalse();
    }

    @Test
    @DisplayName("Should handle subtraction resulting in negative throws exception")
    void shouldHandleSubtractionResultingInNegative() {
        // Given
        Money smaller = new Money(new BigDecimal("50.00"));
        Money larger = new Money(new BigDecimal("100.00"));

        // When & Then: Subtracting larger from smaller results in negative
        assertThatThrownBy(() -> smaller.subtract(larger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be negative");
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        Money original = new Money(new BigDecimal("100.00"));

        // When: Perform operations
        Money added = original.add(new Money(new BigDecimal("50.00")));
        Money multiplied = original.multiply(2);

        // Then: Original is unchanged
        assertThat(original.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(added.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(multiplied.amount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should format toString correctly")
    void shouldFormatToStringCorrectly() {
        // Given
        Money money = new Money(new BigDecimal("1234.56"));

        // Then
        assertThat(money.toString()).isEqualTo("1234.56");
    }

    @Test
    @DisplayName("Should handle zero addition and subtraction")
    void shouldHandleZeroAdditionAndSubtraction() {
        // Given
        Money money = new Money(new BigDecimal("100.00"));
        Money zero = Money.zero();

        // When
        Money addedZero = money.add(zero);
        Money subtractedZero = money.subtract(zero);

        // Then
        assertThat(addedZero.amount()).isEqualByComparingTo(money.amount());
        assertThat(subtractedZero.amount()).isEqualByComparingTo(money.amount());
    }

    @Test
    @DisplayName("Should handle multiplication by zero")
    void shouldHandleMultiplicationByZero() {
        // Given
        Money money = new Money(new BigDecimal("100.00"));

        // When
        Money result = money.multiply(0);

        // Then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.isZero()).isTrue();
    }
}
