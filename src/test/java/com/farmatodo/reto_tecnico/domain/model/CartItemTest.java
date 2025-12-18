package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CartItem domain model.
 * Tests item operations and subtotal calculations.
 */
@DisplayName("CartItem Domain Model Tests")
class CartItemTest {

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .description("Test Description")
                .price(Money.of(new BigDecimal("10000.00")))
                .stock(100)
                .build();
    }

    @Test
    @DisplayName("Should create cart item using builder")
    void shouldCreateCartItemUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("15000.00"));

        // When
        CartItem item = CartItem.builder()
                .id(id)
                .product(testProduct)
                .quantity(3)
                .unitPrice(unitPrice)
                .build();

        // Then
        assertThat(item.getId()).isEqualTo(id);
        assertThat(item.getProduct()).isEqualTo(testProduct);
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
    }

    @Test
    @DisplayName("Should calculate subtotal correctly")
    void shouldCalculateSubtotalCorrectly() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(3)
                .unitPrice(Money.of(new BigDecimal("10000.00")))
                .build();

        // When
        Money subtotal = item.calculateSubtotal();

        // Then - 3 * 10000 = 30000
        assertThat(subtotal.amount()).isEqualByComparingTo(new BigDecimal("30000.00"));
    }

    @Test
    @DisplayName("Should calculate subtotal with single quantity")
    void shouldCalculateSubtotalWithSingleQuantity() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(1)
                .unitPrice(Money.of(new BigDecimal("25000.00")))
                .build();

        // When
        Money subtotal = item.calculateSubtotal();

        // Then
        assertThat(subtotal.amount()).isEqualByComparingTo(new BigDecimal("25000.00"));
    }

    @Test
    @DisplayName("Should increase quantity successfully")
    void shouldIncreaseQuantitySuccessfully() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        // When
        item.increaseQuantity(3);

        // Then
        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw exception when increasing quantity with zero")
    void shouldThrowExceptionWhenIncreasingQuantityWithZero() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        // When & Then
        assertThatThrownBy(() -> item.increaseQuantity(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Additional quantity must be positive");
    }

    @Test
    @DisplayName("Should throw exception when increasing quantity with negative value")
    void shouldThrowExceptionWhenIncreasingQuantityWithNegativeValue() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        // When & Then
        assertThatThrownBy(() -> item.increaseQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Additional quantity must be positive");
    }

    @Test
    @DisplayName("Should update quantity successfully")
    void shouldUpdateQuantitySuccessfully() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        // When
        item.updateQuantity(10);

        // Then
        assertThat(item.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should throw exception when updating quantity to zero")
    void shouldThrowExceptionWhenUpdatingQuantityToZero() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        // When & Then
        assertThatThrownBy(() -> item.updateQuantity(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");
    }

    @Test
    @DisplayName("Should throw exception when updating quantity to negative value")
    void shouldThrowExceptionWhenUpdatingQuantityToNegativeValue() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        // When & Then
        assertThatThrownBy(() -> item.updateQuantity(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");
    }

    @Test
    @DisplayName("Should create cart item using no-args constructor")
    void shouldCreateCartItemUsingNoArgsConstructor() {
        // When
        CartItem item = new CartItem();

        // Then
        assertThat(item.getId()).isNull();
        assertThat(item.getProduct()).isNull();
        assertThat(item.getQuantity()).isZero();
    }

    @Test
    @DisplayName("Should create cart item using all-args constructor")
    void shouldCreateCartItemUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("20000.00"));

        // When
        CartItem item = new CartItem(id, testProduct, 5, unitPrice);

        // Then
        assertThat(item.getId()).isEqualTo(id);
        assertThat(item.getProduct()).isEqualTo(testProduct);
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("10000.00"));

        CartItem item1 = CartItem.builder()
                .id(id)
                .product(testProduct)
                .quantity(2)
                .unitPrice(unitPrice)
                .build();

        CartItem item2 = CartItem.builder()
                .id(id)
                .product(testProduct)
                .quantity(2)
                .unitPrice(unitPrice)
                .build();

        // Then
        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(3)
                .unitPrice(testProduct.getPrice())
                .build();

        // When
        String result = item.toString();

        // Then
        assertThat(result).contains("CartItem");
        assertThat(result).contains("3");
    }

    @Test
    @DisplayName("Should use setters correctly")
    void shouldUseSettersCorrectly() {
        // Given
        CartItem item = new CartItem();
        UUID id = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("12000.00"));

        // When
        item.setId(id);
        item.setProduct(testProduct);
        item.setQuantity(4);
        item.setUnitPrice(unitPrice);

        // Then
        assertThat(item.getId()).isEqualTo(id);
        assertThat(item.getProduct()).isEqualTo(testProduct);
        assertThat(item.getQuantity()).isEqualTo(4);
        assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
    }

    @Test
    @DisplayName("Should update quantity to minimum value of 1")
    void shouldUpdateQuantityToMinimumValueOf1() {
        // Given
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(10)
                .unitPrice(testProduct.getPrice())
                .build();

        // When
        item.updateQuantity(1);

        // Then
        assertThat(item.getQuantity()).isEqualTo(1);
    }
}
