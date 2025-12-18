package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Product domain model.
 * Tests builder, factory methods, stock management, and domain behavior.
 */
@DisplayName("Product Domain Model Tests")
class ProductTest {

    @Test
    @DisplayName("Should create product using builder")
    void shouldCreateProductUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("15000.00"));

        // When
        Product product = Product.builder()
                .id(id)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(price)
                .stock(100)
                .build();

        // Then
        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getName()).isEqualTo("Acetaminofén 500mg");
        assertThat(product.getDescription()).isEqualTo("Analgésico");
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should create product using factory method")
    void shouldCreateProductUsingFactoryMethod() {
        // Given
        Money price = Money.of(new BigDecimal("25000.00"));

        // When
        Product product = Product.create("Ibuprofeno 400mg", "Antiinflamatorio", price, 50);

        // Then
        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("Ibuprofeno 400mg");
        assertThat(product.getDescription()).isEqualTo("Antiinflamatorio");
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should throw exception when creating product with negative stock")
    void shouldThrowExceptionWhenCreatingProductWithNegativeStock() {
        // Given
        Money price = Money.of(new BigDecimal("10000.00"));

        // When & Then
        assertThatThrownBy(() -> Product.create("Test", "Desc", price, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial stock cannot be negative");
    }

    @Test
    @DisplayName("Should check sufficient stock")
    void shouldCheckSufficientStock() {
        // Given
        Product product = Product.builder()
                .stock(10)
                .build();

        // When & Then
        assertThat(product.hasSufficientStock(5)).isTrue();
        assertThat(product.hasSufficientStock(10)).isTrue();
        assertThat(product.hasSufficientStock(11)).isFalse();
    }

    @Test
    @DisplayName("Should check if product is in stock")
    void shouldCheckIfProductIsInStock() {
        // Given
        Product productWithStock = Product.builder().stock(5).build();
        Product productWithoutStock = Product.builder().stock(0).build();

        // When & Then
        assertThat(productWithStock.isInStock()).isTrue();
        assertThat(productWithoutStock.isInStock()).isFalse();
    }

    @Test
    @DisplayName("Should reduce stock successfully")
    void shouldReduceStockSuccessfully() {
        // Given
        Product product = Product.builder()
                .stock(100)
                .build();

        // When
        product.reduceStock(30);

        // Then
        assertThat(product.getStock()).isEqualTo(70);
    }

    @Test
    @DisplayName("Should throw exception when reducing stock with negative quantity")
    void shouldThrowExceptionWhenReducingStockWithNegativeQuantity() {
        // Given
        Product product = Product.builder().stock(10).build();

        // When & Then
        assertThatThrownBy(() -> product.reduceStock(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception when reducing stock below zero")
    void shouldThrowExceptionWhenReducingStockBelowZero() {
        // Given
        Product product = Product.builder()
                .name("Test Product")
                .stock(5)
                .build();

        // When & Then
        assertThatThrownBy(() -> product.reduceStock(10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should increase stock successfully")
    void shouldIncreaseStockSuccessfully() {
        // Given
        Product product = Product.builder()
                .stock(50)
                .build();

        // When
        product.increaseStock(25);

        // Then
        assertThat(product.getStock()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should throw exception when increasing stock with negative quantity")
    void shouldThrowExceptionWhenIncreasingStockWithNegativeQuantity() {
        // Given
        Product product = Product.builder().stock(10).build();

        // When & Then
        assertThatThrownBy(() -> product.increaseStock(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should update product info")
    void shouldUpdateProductInfo() {
        // Given
        Product product = Product.builder()
                .name("Old Name")
                .description("Old Description")
                .price(Money.of(new BigDecimal("10000.00")))
                .build();

        Money newPrice = Money.of(new BigDecimal("15000.00"));

        // When
        product.updateInfo("New Name", "New Description", newPrice);

        // Then
        assertThat(product.getName()).isEqualTo("New Name");
        assertThat(product.getDescription()).isEqualTo("New Description");
        assertThat(product.getPrice()).isEqualTo(newPrice);
    }

    @Test
    @DisplayName("Should calculate total for quantity")
    void shouldCalculateTotalForQuantity() {
        // Given
        Product product = Product.builder()
                .price(Money.of(new BigDecimal("5000.00")))
                .build();

        // When
        Money total = product.calculateTotal(3);

        // Then
        assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("Should throw exception when calculating total with negative quantity")
    void shouldThrowExceptionWhenCalculatingTotalWithNegativeQuantity() {
        // Given
        Product product = Product.builder()
                .price(Money.of(new BigDecimal("5000.00")))
                .build();

        // When & Then
        assertThatThrownBy(() -> product.calculateTotal(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should create product using no-args constructor")
    void shouldCreateProductUsingNoArgsConstructor() {
        // When
        Product product = new Product();

        // Then
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isNull();
    }

    @Test
    @DisplayName("Should create product using all-args constructor")
    void shouldCreateProductUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("20000.00"));

        // When
        Product product = new Product(id, "AllArgs Product", "Description", price, 75);

        // Then
        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getName()).isEqualTo("AllArgs Product");
        assertThat(product.getStock()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        Money price = Money.of(new BigDecimal("10000.00"));

        Product product1 = Product.builder()
                .id(id)
                .name("Test")
                .price(price)
                .stock(10)
                .build();

        Product product2 = Product.builder()
                .id(id)
                .name("Test")
                .price(price)
                .stock(10)
                .build();

        // Then
        assertThat(product1).isEqualTo(product2);
        assertThat(product1.hashCode()).isEqualTo(product2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        Product product = Product.builder()
                .name("ToString Product")
                .stock(50)
                .build();

        // When
        String result = product.toString();

        // Then
        assertThat(result).contains("ToString Product");
        assertThat(result).contains("50");
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        // Given
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("No Description Product")
                .price(Money.of(new BigDecimal("5000.00")))
                .stock(10)
                .build();

        // Then
        assertThat(product.getDescription()).isNull();
    }
}
