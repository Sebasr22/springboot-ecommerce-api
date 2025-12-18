package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.CartNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.EmptyCartException;
import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.*;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.in.AddToCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.CheckoutCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.GetCartUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CartRestMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for CartController.
 * Tests shopping cart operations: add to cart, get cart, and checkout.
 */
@WebMvcTest(CartController.class)
@Import(CartRestMapperImpl.class)
@DisplayName("CartController REST Tests")
class CartControllerTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "default-dev-key-change-in-production";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddToCartUseCase addToCartUseCase;

    @MockBean
    private GetCartUseCase getCartUseCase;

    @MockBean
    private CheckoutCartUseCase checkoutCartUseCase;

    private UUID customerId;
    private UUID productId;
    private Product testProduct;
    private Cart testCart;
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

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        testCart = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .items(List.of(cartItem))
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
    @DisplayName("Should add product to cart successfully with 201 status")
    void shouldAddToCartSuccessfully() throws Exception {
        // Given: Valid add to cart request
        String requestBody = String.format("""
            {
                "customerId": "%s",
                "productId": "%s",
                "quantity": 2
            }
            """, customerId, productId);

        // Mock successful add to cart
        when(addToCartUseCase.addToCart(customerId, productId, 2)).thenReturn(testCart);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalAmount").value(20000.0))
                .andExpect(jsonPath("$.totalItemCount").value(2));

        // Verify use case was called
        verify(addToCartUseCase, times(1)).addToCart(customerId, productId, 2);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when customerId is null")
    void shouldReturn400WhenCustomerIdNull() throws Exception {
        // Given: Request with null customerId
        String requestBody = String.format("""
            {
                "productId": "%s",
                "quantity": 2
            }
            """, productId);

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.customerId").exists());

        // Use case should NOT be called
        verify(addToCartUseCase, never()).addToCart(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when productId is null")
    void shouldReturn400WhenProductIdNull() throws Exception {
        // Given: Request with null productId
        String requestBody = String.format("""
            {
                "customerId": "%s",
                "quantity": 2
            }
            """, customerId);

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.productId").exists());

        verify(addToCartUseCase, never()).addToCart(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when quantity is less than 1")
    void shouldReturn400WhenQuantityLessThanOne() throws Exception {
        // Given: Request with quantity = 0
        String requestBody = String.format("""
            {
                "customerId": "%s",
                "productId": "%s",
                "quantity": 0
            }
            """, customerId, productId);

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.quantity").value(containsString("at least 1")));

        verify(addToCartUseCase, never()).addToCart(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should return 404 Not Found when product doesn't exist")
    void shouldReturn404WhenProductNotFound() throws Exception {
        // Given: Request with non-existent product
        UUID nonExistentProductId = UUID.randomUUID();
        String requestBody = String.format("""
            {
                "customerId": "%s",
                "productId": "%s",
                "quantity": 2
            }
            """, customerId, nonExistentProductId);

        // Mock product not found
        when(addToCartUseCase.addToCart(customerId, nonExistentProductId, 2))
                .thenThrow(new ProductNotFoundException(nonExistentProductId));

        // When & Then: Verify 404 status
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));

        verify(addToCartUseCase, times(1)).addToCart(customerId, nonExistentProductId, 2);
    }

    @Test
    @DisplayName("Should return 409 Conflict when stock is insufficient")
    void shouldReturn409WhenInsufficientStock() throws Exception {
        // Given: Request with quantity exceeding stock
        String requestBody = String.format("""
            {
                "customerId": "%s",
                "productId": "%s",
                "quantity": 500
            }
            """, customerId, productId);

        // Mock insufficient stock exception
        when(addToCartUseCase.addToCart(customerId, productId, 500))
                .thenThrow(new InsufficientStockException(productId, "Acetaminofén 500mg", 100, 500));

        // When & Then: Verify 409 status
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));

        verify(addToCartUseCase, times(1)).addToCart(customerId, productId, 500);
    }

    // ==================== GET CART TESTS ====================

    @Test
    @DisplayName("Should get cart successfully with 200 status")
    void shouldGetCartSuccessfully() throws Exception {
        // Given: Existing cart
        when(getCartUseCase.getCart(customerId)).thenReturn(testCart);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(get("/api/v1/cart/{customerId}", customerId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Acetaminofén 500mg"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(20000.0))
                .andExpect(jsonPath("$.totalItemCount").value(2));

        verify(getCartUseCase, times(1)).getCart(customerId);
    }

    @Test
    @DisplayName("Should get empty cart when no cart exists (auto-creation)")
    void shouldGetEmptyCartWhenNoCartExists() throws Exception {
        // Given: No cart exists - service returns new empty cart
        Cart emptyCart = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(getCartUseCase.getCart(customerId)).thenReturn(emptyCart);

        // When & Then: Should return 200 with empty cart
        mockMvc.perform(get("/api/v1/cart/{customerId}", customerId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalItemCount").value(0));

        verify(getCartUseCase, times(1)).getCart(customerId);
    }

    // ==================== CHECKOUT TESTS ====================

    @Test
    @DisplayName("Should checkout successfully with 201 status")
    void shouldCheckoutSuccessfully() throws Exception {
        // Given: Valid cart ready for checkout
        when(checkoutCartUseCase.checkout(customerId)).thenReturn(testOrder);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/cart/checkout/{customerId}", customerId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(testOrder.getId().toString()))
                .andExpect(jsonPath("$.message").value(containsString("Checkout successful")));

        verify(checkoutCartUseCase, times(1)).checkout(customerId);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when cart is empty")
    void shouldReturn400WhenCartIsEmpty() throws Exception {
        // Given: Empty cart
        when(checkoutCartUseCase.checkout(customerId))
                .thenThrow(new EmptyCartException());

        // When & Then: Verify 400 status
        mockMvc.perform(post("/api/v1/cart/checkout/{customerId}", customerId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("EMPTY_CART"))
                .andExpect(jsonPath("$.message").value(containsString("empty cart")));

        verify(checkoutCartUseCase, times(1)).checkout(customerId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when cart doesn't exist for checkout")
    void shouldReturn404WhenCartNotFoundForCheckout() throws Exception {
        // Given: Non-existent cart
        UUID nonExistentCustomerId = UUID.randomUUID();
        when(checkoutCartUseCase.checkout(nonExistentCustomerId))
                .thenThrow(new CartNotFoundException(nonExistentCustomerId));

        // When & Then: Verify 404 status
        mockMvc.perform(post("/api/v1/cart/checkout/{customerId}", nonExistentCustomerId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("CART_NOT_FOUND"));

        verify(checkoutCartUseCase, times(1)).checkout(nonExistentCustomerId);
    }

    @Test
    @DisplayName("Should return 409 Conflict when stock becomes insufficient during checkout")
    void shouldReturn409WhenInsufficientStockDuringCheckout() throws Exception {
        // Given: Stock changed between adding to cart and checkout
        when(checkoutCartUseCase.checkout(customerId))
                .thenThrow(new InsufficientStockException(productId, "Acetaminofén 500mg", 1, 2));

        // When & Then: Verify 409 status
        mockMvc.perform(post("/api/v1/cart/checkout/{customerId}", customerId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));

        verify(checkoutCartUseCase, times(1)).checkout(customerId);
    }
}
