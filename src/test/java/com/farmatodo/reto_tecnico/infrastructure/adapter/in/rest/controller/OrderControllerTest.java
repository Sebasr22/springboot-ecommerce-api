package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.in.CreateOrderUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.SearchProductUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CustomerRestMapperImpl;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.OrderItemRestMapperImpl;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.OrderRestMapperImpl;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.ProductRestMapperImpl;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for OrderController.
 * Tests order creation, retrieval, and exception handling.
 *
 * Imports multiple MapStruct mappers for coverage.
 */
@WebMvcTest(OrderController.class)
@Import({
    OrderRestMapperImpl.class,
    OrderItemRestMapperImpl.class,
    ProductRestMapperImpl.class,
    CustomerRestMapperImpl.class
})
@DisplayName("OrderController REST Tests")
class OrderControllerTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "default-dev-key-change-in-production";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private SearchProductUseCase searchProductUseCase;

    @MockBean
    private OrderRepositoryPort orderRepository;

    @MockBean
    private com.farmatodo.reto_tecnico.application.service.AuditLogService auditLogService;

    private Product testProduct;
    private Customer testCustomer;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(new Email("juan@test.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123, Bogotá")
                .build();

        OrderItem orderItem = OrderItem.create(testProduct, 2);
        testOrder = Order.create(testCustomer, List.of(orderItem));
    }

    @Test
    @DisplayName("Should create order successfully with 201 status")
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given: Valid order request
        UUID productId = testProduct.getId();
        String requestBody = String.format("""
            {
                "customerName": "Juan Pérez",
                "customerEmail": "juan@test.com",
                "customerPhone": "3001234567",
                "customerAddress": "Calle 123, Bogotá",
                "items": [
                    {
                        "productId": "%s",
                        "quantity": 2
                    }
                ]
            }
            """, productId);

        // Mock product search
        when(searchProductUseCase.findById(productId)).thenReturn(Optional.of(testProduct));

        // Mock order creation (with new overloaded method signature)
        when(createOrderUseCase.createOrder(any(Customer.class), anyList(), any())).thenReturn(testOrder);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/orders")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Juan Pérez"))
                .andExpect(jsonPath("$.customerEmail").value("juan@test.com"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").exists());

        // Verify service calls
        verify(searchProductUseCase, times(1)).findById(productId);
        verify(createOrderUseCase, times(1)).createOrder(any(Customer.class), anyList(), any());
    }

    @Test
    @DisplayName("Should return 409 Conflict when stock is insufficient")
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        // Given: Order request with quantity exceeding stock
        UUID productId = testProduct.getId();
        String requestBody = String.format("""
            {
                "customerName": "Juan Pérez",
                "customerEmail": "juan@test.com",
                "customerPhone": "3001234567",
                "customerAddress": "Calle 123, Bogotá",
                "items": [
                    {
                        "productId": "%s",
                        "quantity": 500
                    }
                ]
            }
            """, productId);

        // Mock product search
        when(searchProductUseCase.findById(productId)).thenReturn(Optional.of(testProduct));

        // Mock insufficient stock exception (with new overloaded method signature)
        when(createOrderUseCase.createOrder(any(Customer.class), anyList(), any()))
                .thenThrow(new InsufficientStockException(productId, "Acetaminofén 500mg", 100, 500));

        // When & Then: Verify 409 status and error message
        mockMvc.perform(post("/api/v1/orders")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value(containsString("Insufficient stock")));

        verify(createOrderUseCase, times(1)).createOrder(any(Customer.class), anyList(), any());
    }

    @Test
    @DisplayName("Should get order by ID successfully with 200 status")
    void shouldGetOrderById() throws Exception {
        // Given: Existing order
        UUID orderId = testOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then: Call endpoint and verify response
        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerName").value("Juan Pérez"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when order doesn't exist")
    void shouldReturnNotFoundWhenOrderDoesntExist() throws Exception {
        // Given: Non-existent order ID
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId))
                .thenThrow(new OrderNotFoundException(orderId));

        // When & Then: Verify 404 status
        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString(orderId.toString())));

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when product doesn't exist")
    void shouldReturnNotFoundWhenProductDoesntExist() throws Exception {
        // Given: Order with non-existent product
        UUID nonExistentProductId = UUID.randomUUID();
        String requestBody = String.format("""
            {
                "customerName": "Juan Pérez",
                "customerEmail": "juan@test.com",
                "customerPhone": "3001234567",
                "customerAddress": "Calle 123, Bogotá",
                "items": [
                    {
                        "productId": "%s",
                        "quantity": 2
                    }
                ]
            }
            """, nonExistentProductId);

        // Mock product not found
        when(searchProductUseCase.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // When & Then: Verify 404 status
        mockMvc.perform(post("/api/v1/orders")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));

        verify(searchProductUseCase, times(1)).findById(nonExistentProductId);
        verify(createOrderUseCase, never()).createOrder(any(Customer.class), anyList(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when items list is empty")
    void shouldReturnBadRequestWhenItemsEmpty() throws Exception {
        // Given: Order request with empty items
        String requestBody = """
            {
                "customerName": "Juan Pérez",
                "customerEmail": "juan@test.com",
                "customerPhone": "3001234567",
                "customerAddress": "Calle 123, Bogotá",
                "items": []
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/orders")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.items").value(containsString("at least one")));

        verify(searchProductUseCase, never()).findById(any());
        verify(createOrderUseCase, never()).createOrder(any(Customer.class), anyList(), any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is invalid")
    void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
        // Given: Order request with invalid email
        String requestBody = """
            {
                "customerName": "Juan Pérez",
                "customerEmail": "invalid-email",
                "customerPhone": "3001234567",
                "customerAddress": "Calle 123, Bogotá",
                "items": [
                    {
                        "productId": "123e4567-e89b-12d3-a456-426614174000",
                        "quantity": 2
                    }
                ]
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/orders")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.customerEmail").exists());

        verify(createOrderUseCase, never()).createOrder(any(Customer.class), anyList(), any());
    }
}
