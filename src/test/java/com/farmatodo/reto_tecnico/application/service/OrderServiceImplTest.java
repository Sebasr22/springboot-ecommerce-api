package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl.
 * Tests order creation with stock validation and persistence.
 *
 * Uses pure unit testing with Mockito (NO Spring context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer testCustomer;
    private Product testProduct;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(new Email("juan@test.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123 #45-67, Bogotá")
                .build();

        // Create test product with sufficient stock
        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico y antipirético")
                .price(new Money(new BigDecimal("12500.00")))
                .stock(100)
                .build();

        // Create test order item
        testOrderItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when product has insufficient stock")
    void shouldThrowInsufficientStockExceptionWhenStockIsInsufficient() {
        // Given: Product with low stock
        Product lowStockProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(1) // Only 1 unit available
                .build();

        OrderItem itemWithHighQuantity = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(lowStockProduct)
                .quantity(5) // Requesting 5 units
                .unitPrice(lowStockProduct.getPrice())
                .build();

        // Mock customer exists
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        // Mock product repository
        when(productRepository.findById(lowStockProduct.getId()))
                .thenReturn(Optional.of(lowStockProduct));

        // Act & Assert
        assertThatThrownBy(() ->
                orderService.createOrder(testCustomer, List.of(itemWithHighQuantity))
        )
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Ibuprofeno 400mg")
                .hasMessageContaining("Available: 1")
                .hasMessageContaining("Requested: 5");

        // Verify order was never saved
        verify(orderRepository, never()).save(any(Order.class));

        // Verify stock was never updated
        verify(productRepository, never()).updateStock(any(UUID.class), anyInt());
    }

    @Test
    @DisplayName("Should create order successfully when stock is sufficient")
    void shouldCreateOrderSuccessfullyWhenStockIsSufficient() {
        // Given: Customer exists, product has sufficient stock
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        // Mock order save
        Order mockSavedOrder = Order.create(testCustomer, List.of(testOrderItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);

        // Mock stock update
        when(productRepository.updateStock(any(UUID.class), anyInt())).thenReturn(testProduct);

        // Act
        Order result = orderService.createOrder(testCustomer, List.of(testOrderItem));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomer()).isEqualTo(testCustomer);
        assertThat(result.getItems()).hasSize(1);

        // Verify order was saved
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verify stock was reduced
        int expectedNewStock = testProduct.getStock() - testOrderItem.getQuantity();
        verify(productRepository, times(1))
                .updateStock(testProduct.getId(), expectedNewStock);
    }

    @Test
    @DisplayName("Should create customer if not exists")
    void shouldCreateCustomerIfNotExists() {
        // Given: Customer does not exist in repository
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.empty());

        // Mock customer save
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        // Mock product repository
        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        // Mock order save
        Order mockSavedOrder = Order.create(testCustomer, List.of(testOrderItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);

        // Mock stock update
        when(productRepository.updateStock(any(UUID.class), anyInt())).thenReturn(testProduct);

        // Act
        Order result = orderService.createOrder(testCustomer, List.of(testOrderItem));

        // Assert
        assertThat(result).isNotNull();

        // Verify customer was created
        verify(customerRepository, times(1)).save(testCustomer);

        // Verify order was saved
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should validate stock for multiple items")
    void shouldValidateStockForMultipleItems() {
        // Given: Two products, one with insufficient stock
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Product 1")
                .description("Description 1")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(10)
                .build();

        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Product 2")
                .description("Description 2")
                .price(new Money(new BigDecimal("20000.00")))
                .stock(2) // Low stock
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product1)
                .quantity(5)
                .unitPrice(product1.getPrice())
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product2)
                .quantity(10) // Exceeds available stock
                .unitPrice(product2.getPrice())
                .build();

        // Mock customer exists
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        // Mock product repositories
        when(productRepository.findById(product1.getId()))
                .thenReturn(Optional.of(product1));
        when(productRepository.findById(product2.getId()))
                .thenReturn(Optional.of(product2));

        // Act & Assert
        assertThatThrownBy(() ->
                orderService.createOrder(testCustomer, List.of(item1, item2))
        )
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Product 2");

        // Verify order was never saved
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should use customer's default address when no explicit delivery address is provided")
    void shouldUseCustomerAddressWhenNoExplicitDeliveryAddressProvided() {
        // Given: Customer with default address
        String expectedAddress = "Calle 123 #45-67, Bogotá";
        assertThat(testCustomer.getAddress()).isEqualTo(expectedAddress);

        // Mock customer exists
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        // Mock product repository
        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        // Mock order save (capture the saved order)
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock stock update
        when(productRepository.updateStock(any(UUID.class), anyInt())).thenReturn(testProduct);

        // Act: Create order WITHOUT explicit delivery address (null)
        Order result = orderService.createOrder(testCustomer, List.of(testOrderItem), null);

        // Assert: Delivery address should be customer's default address
        assertThat(result).isNotNull();
        assertThat(result.getDeliveryAddress()).isEqualTo(expectedAddress);
        assertThat(result.getDeliveryAddress()).isEqualTo(testCustomer.getAddress());

        // Verify order was saved
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should use explicit delivery address when provided")
    void shouldUseExplicitDeliveryAddressWhenProvided() {
        // Given: Customer with default address and explicit delivery address
        String customerDefaultAddress = "Calle 123 #45-67, Bogotá";
        String explicitDeliveryAddress = "Calle 456 #78-90, Medellín";

        assertThat(testCustomer.getAddress()).isEqualTo(customerDefaultAddress);
        assertThat(explicitDeliveryAddress).isNotEqualTo(customerDefaultAddress);

        // Mock customer exists
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        // Mock product repository
        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        // Mock order save (capture the saved order)
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock stock update
        when(productRepository.updateStock(any(UUID.class), anyInt())).thenReturn(testProduct);

        // Act: Create order WITH explicit delivery address
        Order result = orderService.createOrder(testCustomer, List.of(testOrderItem), explicitDeliveryAddress);

        // Assert: Delivery address should be the explicit one, NOT customer's default
        assertThat(result).isNotNull();
        assertThat(result.getDeliveryAddress()).isEqualTo(explicitDeliveryAddress);
        assertThat(result.getDeliveryAddress()).isNotEqualTo(testCustomer.getAddress());

        // Verify order was saved
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should use customer's address when explicit delivery address is blank")
    void shouldUseCustomerAddressWhenExplicitDeliveryAddressIsBlank() {
        // Given: Customer with default address and blank explicit delivery address
        String expectedAddress = "Calle 123 #45-67, Bogotá";
        String blankDeliveryAddress = "   "; // Blank string

        // Mock customer exists
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        // Mock product repository
        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));

        // Mock order save (capture the saved order)
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock stock update
        when(productRepository.updateStock(any(UUID.class), anyInt())).thenReturn(testProduct);

        // Act: Create order with BLANK explicit delivery address
        Order result = orderService.createOrder(testCustomer, List.of(testOrderItem), blankDeliveryAddress);

        // Assert: Delivery address should fallback to customer's default address
        assertThat(result).isNotNull();
        assertThat(result.getDeliveryAddress()).isEqualTo(expectedAddress);
        assertThat(result.getDeliveryAddress()).isEqualTo(testCustomer.getAddress());

        // Verify order was saved
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
