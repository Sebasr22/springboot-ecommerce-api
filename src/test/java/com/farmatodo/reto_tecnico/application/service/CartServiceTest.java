package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.CartNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.EmptyCartException;
import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.*;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.in.CreateOrderUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.CartRepositoryPort;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartService.
 * Tests cart operations: add to cart, get cart, and checkout.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepositoryPort cartRepository;

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private CreateOrderUseCase orderService;

    @InjectMocks
    private CartService cartService;

    private UUID customerId;
    private UUID productId;
    private Product testProduct;
    private Cart existingCart;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testProduct = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        existingCart = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Customer testCustomer = Customer.builder()
                .id(customerId)
                .name("Juan Pérez")
                .email(new Email("juan@test.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123, Bogotá")
                .build();

        OrderItem orderItem = OrderItem.create(testProduct, 2);
        testOrder = Order.create(testCustomer, List.of(orderItem));
    }

    // ==================== ADD TO CART TESTS ====================

    @Test
    @DisplayName("Should add first product to cart successfully")
    void shouldAddFirstProductToCart() {
        // Given: No existing cart, valid product
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Cart result = cartService.addToCart(customerId, productId, 2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProduct().getId()).isEqualTo(productId);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);

        verify(cartRepository, times(2)).save(any(Cart.class)); // Once for new cart, once after adding item
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should increment quantity when product already in cart")
    void shouldIncrementQuantityWhenProductAlreadyInCart() {
        // Given: Cart with existing product
        CartItem existingItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(3)
                .unitPrice(testProduct.getPrice())
                .build();
        existingCart.getItems().add(existingItem);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Cart result = cartService.addToCart(customerId, productId, 2);

        // Then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5); // 3 + 2

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when quantity is zero")
    void shouldThrowExceptionWhenQuantityIsZero() {
        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(customerId, productId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        verify(cartRepository, never()).save(any(Cart.class));
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when quantity is negative")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(customerId, productId, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(customerId, productId, 2))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given: Product with low stock
        Product lowStockProduct = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(5) // Only 5 in stock
                .build();

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(lowStockProduct));

        // When & Then: Try to add 10 items
        assertThatThrownBy(() -> cartService.addToCart(customerId, productId, 10))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should consider existing cart quantity for stock validation")
    void shouldConsiderExistingCartQuantityForStockValidation() {
        // Given: Cart already has 90 items of a product with 100 stock
        Product limitedStockProduct = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        CartItem existingItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(limitedStockProduct)
                .quantity(90)
                .unitPrice(limitedStockProduct.getPrice())
                .build();
        existingCart.getItems().add(existingItem);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(limitedStockProduct));

        // When & Then: Try to add 15 more (total 105 > 100 stock)
        assertThatThrownBy(() -> cartService.addToCart(customerId, productId, 15))
                .isInstanceOf(InsufficientStockException.class);
    }

    // ==================== GET CART TESTS ====================

    @Test
    @DisplayName("Should get existing cart")
    void shouldGetExistingCart() {
        // Given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));

        // When
        Cart result = cartService.getCart(customerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);

        verify(cartRepository, times(1)).findByCustomerId(customerId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should create new cart if not exists")
    void shouldCreateNewCartIfNotExists() {
        // Given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Cart result = cartService.getCart(customerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getItems()).isEmpty();

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // ==================== CHECKOUT TESTS ====================

    @Test
    @DisplayName("Should checkout successfully")
    void shouldCheckoutSuccessfully() {
        // Given: Cart with items
        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();
        existingCart.getItems().add(cartItem);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));
        when(orderService.createOrder(any(UUID.class), ArgumentMatchers.<List<OrderItem>>any())).thenReturn(testOrder);

        // When
        Order result = cartService.checkout(customerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrder.getId());

        // Verify cart was cleared
        verify(cartRepository, times(1)).deleteByCustomerId(customerId);
        verify(orderService, times(1)).createOrder(any(UUID.class), ArgumentMatchers.<List<OrderItem>>any());
    }

    @Test
    @DisplayName("Should throw exception when checkout with cart not found")
    void shouldThrowExceptionWhenCheckoutWithCartNotFound() {
        // Given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.checkout(customerId))
                .isInstanceOf(CartNotFoundException.class);

        verify(orderService, never()).createOrder(any(UUID.class), ArgumentMatchers.<List<OrderItem>>any());
        verify(cartRepository, never()).deleteByCustomerId(any());
    }

    @Test
    @DisplayName("Should throw exception when checkout with empty cart")
    void shouldThrowExceptionWhenCheckoutWithEmptyCart() {
        // Given: Empty cart
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));

        // When & Then
        assertThatThrownBy(() -> cartService.checkout(customerId))
                .isInstanceOf(EmptyCartException.class);

        verify(orderService, never()).createOrder(any(UUID.class), ArgumentMatchers.<List<OrderItem>>any());
        verify(cartRepository, never()).deleteByCustomerId(any());
    }

    @Test
    @DisplayName("Should convert cart items to order items correctly")
    void shouldConvertCartItemsToOrderItemsCorrectly() {
        // Given: Cart with multiple items
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(50)
                .build();

        CartItem item1 = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        CartItem item2 = CartItem.builder()
                .id(UUID.randomUUID())
                .product(product2)
                .quantity(3)
                .unitPrice(product2.getPrice())
                .build();

        existingCart.getItems().add(item1);
        existingCart.getItems().add(item2);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));
        when(orderService.createOrder(any(UUID.class), ArgumentMatchers.<List<OrderItem>>any())).thenReturn(testOrder);

        // When
        cartService.checkout(customerId);

        // Then: Capture the order items passed to orderService
        ArgumentCaptor<List<OrderItem>> orderItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderService).createOrder(any(UUID.class), orderItemsCaptor.capture());

        List<OrderItem> capturedItems = orderItemsCaptor.getValue();
        assertThat(capturedItems).hasSize(2);
    }
}
