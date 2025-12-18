package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Cart domain model (Aggregate Root).
 * Tests cart operations and domain behavior.
 */
@DisplayName("Cart Domain Model Tests")
class CartTest {

    private Cart cart;
    private Product product1;
    private Product product2;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        cart = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(Money.of(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(Money.of(new BigDecimal("15000.00")))
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("Should create cart using builder")
    void shouldCreateCartUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        Cart newCart = Cart.builder()
                .id(id)
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(newCart.getId()).isEqualTo(id);
        assertThat(newCart.getCustomerId()).isEqualTo(customerId);
        assertThat(newCart.getItems()).isEmpty();
        assertThat(newCart.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should add product to cart")
    void shouldAddProductToCart() {
        // When
        cart.addProduct(product1, 2);

        // Then
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct()).isEqualTo(product1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should increase quantity when adding existing product")
    void shouldIncreaseQuantityWhenAddingExistingProduct() {
        // Given
        cart.addProduct(product1, 2);

        // When
        cart.addProduct(product1, 3);

        // Then
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should add multiple different products")
    void shouldAddMultipleDifferentProducts() {
        // When
        cart.addProduct(product1, 2);
        cart.addProduct(product2, 1);

        // Then
        assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception when adding null product")
    void shouldThrowExceptionWhenAddingNullProduct() {
        // When & Then
        assertThatThrownBy(() -> cart.addProduct(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when adding product with zero quantity")
    void shouldThrowExceptionWhenAddingProductWithZeroQuantity() {
        // When & Then
        assertThatThrownBy(() -> cart.addProduct(product1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    @DisplayName("Should throw exception when adding product with negative quantity")
    void shouldThrowExceptionWhenAddingProductWithNegativeQuantity() {
        // When & Then
        assertThatThrownBy(() -> cart.addProduct(product1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    @DisplayName("Should remove product from cart")
    void shouldRemoveProductFromCart() {
        // Given
        cart.addProduct(product1, 2);
        cart.addProduct(product2, 1);

        // When
        boolean removed = cart.removeProduct(product1.getId());

        // Then
        assertThat(removed).isTrue();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct()).isEqualTo(product2);
    }

    @Test
    @DisplayName("Should return false when removing non-existent product")
    void shouldReturnFalseWhenRemovingNonExistentProduct() {
        // Given
        cart.addProduct(product1, 2);

        // When
        boolean removed = cart.removeProduct(UUID.randomUUID());

        // Then
        assertThat(removed).isFalse();
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should update product quantity")
    void shouldUpdateProductQuantity() {
        // Given
        cart.addProduct(product1, 2);

        // When
        boolean updated = cart.updateProductQuantity(product1.getId(), 5);

        // Then
        assertThat(updated).isTrue();
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return false when updating non-existent product")
    void shouldReturnFalseWhenUpdatingNonExistentProduct() {
        // Given
        cart.addProduct(product1, 2);

        // When
        boolean updated = cart.updateProductQuantity(UUID.randomUUID(), 5);

        // Then
        assertThat(updated).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when updating to invalid quantity")
    void shouldThrowExceptionWhenUpdatingToInvalidQuantity() {
        // Given
        cart.addProduct(product1, 2);

        // When & Then
        assertThatThrownBy(() -> cart.updateProductQuantity(product1.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");

        assertThatThrownBy(() -> cart.updateProductQuantity(product1.getId(), -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be at least 1");
    }

    @Test
    @DisplayName("Should clear cart")
    void shouldClearCart() {
        // Given
        cart.addProduct(product1, 2);
        cart.addProduct(product2, 1);

        // When
        cart.clear();

        // Then
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should calculate total correctly")
    void shouldCalculateTotalCorrectly() {
        // Given
        cart.addProduct(product1, 2);  // 2 * 10000 = 20000
        cart.addProduct(product2, 1);  // 1 * 15000 = 15000

        // When
        Money total = cart.calculateTotal();

        // Then - Total: 35000
        assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("35000.00"));
    }

    @Test
    @DisplayName("Should return zero total for empty cart")
    void shouldReturnZeroTotalForEmptyCart() {
        // When
        Money total = cart.calculateTotal();

        // Then
        assertThat(total.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should check if cart is empty")
    void shouldCheckIfCartIsEmpty() {
        // Given
        assertThat(cart.isEmpty()).isTrue();

        // When
        cart.addProduct(product1, 1);

        // Then
        assertThat(cart.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Should get total item count")
    void shouldGetTotalItemCount() {
        // Given
        cart.addProduct(product1, 2);
        cart.addProduct(product2, 3);

        // When
        int totalCount = cart.getTotalItemCount();

        // Then - 2 + 3 = 5
        assertThat(totalCount).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return zero item count for empty cart")
    void shouldReturnZeroItemCountForEmptyCart() {
        // When
        int totalCount = cart.getTotalItemCount();

        // Then
        assertThat(totalCount).isZero();
    }

    @Test
    @DisplayName("Should create cart using no-args constructor")
    void shouldCreateCartUsingNoArgsConstructor() {
        // When
        Cart newCart = new Cart();

        // Then
        assertThat(newCart.getId()).isNull();
        assertThat(newCart.getCustomerId()).isNull();
    }

    @Test
    @DisplayName("Should create cart using all-args constructor")
    void shouldCreateCartUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ArrayList<CartItem> items = new ArrayList<>();
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime updated = LocalDateTime.now();

        // When
        Cart newCart = new Cart(id, customerId, items, created, updated);

        // Then
        assertThat(newCart.getId()).isEqualTo(id);
        assertThat(newCart.getCustomerId()).isEqualTo(customerId);
        assertThat(newCart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Cart cart1 = Cart.builder()
                .id(id)
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(now)
                .build();

        Cart cart2 = Cart.builder()
                .id(id)
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(now)
                .build();

        // Then
        assertThat(cart1).isEqualTo(cart2);
        assertThat(cart1.hashCode()).isEqualTo(cart2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // When
        String result = cart.toString();

        // Then
        assertThat(result).contains("Cart");
        assertThat(result).contains(customerId.toString());
    }

    @Test
    @DisplayName("Should use setters correctly")
    void shouldUseSettersCorrectly() {
        // Given
        Cart newCart = new Cart();
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        newCart.setId(id);
        newCart.setCustomerId(customerId);
        newCart.setItems(new ArrayList<>());
        newCart.setCreatedAt(now);
        newCart.setUpdatedAt(now);

        // Then
        assertThat(newCart.getId()).isEqualTo(id);
        assertThat(newCart.getCustomerId()).isEqualTo(customerId);
        assertThat(newCart.getCreatedAt()).isEqualTo(now);
        assertThat(newCart.getUpdatedAt()).isEqualTo(now);
    }
}
